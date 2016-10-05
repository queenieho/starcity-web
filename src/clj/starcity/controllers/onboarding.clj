(ns starcity.controllers.onboarding
  (:require [starcity.views.onboarding :as view]
            [starcity.models.stripe :as stripe]
            [starcity.models.onboarding :as onboarding]
            [starcity.controllers.utils :refer :all]
            [starcity.config.stripe :as config]
            [starcity.util :refer :all]
            [ring.util.response :as response]
            [ring.util.codec :refer [url-encode]]
            [compojure.core :refer [context defroutes GET POST]]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [taoensso.timbre :as timbre]))

;; =============================================================================
;; Helpers
;; =============================================================================

(def ^:private default-error-message
  "Whoops! Something went wrong. Please contact us at team@joinstarcity.com.")

(def ^:private payment-methods
  {"ach"   :security-deposit.payment-method/ach
   "check" :security-deposit.payment-method/check})

(def ^:private payment-types
  {"full"    :security-deposit.payment-type/full
   "partial" :security-deposit.payment-type/partial})

;; =============================================================================
;; Access Rules

;; TODO: Are these really needed?
(defn- can-choose-payment-method?
  "Can only choose payment method when a payment is not yet received and a
  Stripe customer is not yet created."
  [progress]
  (and (not (onboarding/payment-received? progress))
       (not (onboarding/customer-created? progress))))

(defn- can-make-payment?*
  "Convenience function to construct `can-make-payment-by-X?` functions."
  [progress method]
  (and (= (onboarding/payment-method progress) method)
       (not (onboarding/payment-received? progress))))

(defn- can-make-payment-by-check?
  "Can only make a payment by check if a payment has not yet been received AND the
  chosen payment method is `check`."
  [progress]
  (can-make-payment?* progress :security-deposit.payment-method/check))

(defn- can-make-payment-by-ach?
  "Can only make a payment by ACH if a payment has not yet been received AND the
  chosen payment method is `ach`."
  [progress]
  (and (can-make-payment?* progress :security-deposit.payment-method/ach)
       (onboarding/bank-account-verified? progress)))

;; =============================================================================
;; Routing to Appropriate Step

(def ^:private should-choose-payment-method?
  (comp not onboarding/payment-method-chosen?))

(defn- ach?
  [progress]
  (= (onboarding/payment-method progress) :security-deposit.payment-method/ach))

(defn- awaiting-payment-by-check?
  [progress]
  (and (= (onboarding/payment-method progress) :security-deposit.payment-method/check)
       (not (onboarding/payment-received? progress))))

(defn- should-enter-bank-information?
  "User should enter bank information iff ach is chosen but no Stripe customer
  is created yet."
  [progress]
  (and (ach? progress)
       (not (onboarding/customer-created? progress))))

(defn- should-verify-microdeposits?
  "User should verify bank account iff chosen payment method is ACH and his/her
  bank account is not yet verified."
  [progress]
  (and (ach? progress)
       (not (onboarding/bank-account-verified? progress))))

(defn- should-pay-security-deposit?
  "User should pay security deposit iff his/her bank account is verified and
  the security deposit is not yet paid."
  [progress]
  (and (onboarding/bank-account-verified? progress)
       (not (onboarding/payment-received? progress))))

(defn- security-deposit-finished?
  "Is the security deposit process finished?"
  [progress]
  (onboarding/payment-received? progress))

(defn- current-step
  [progress]
  ;; NOTE: Order is important here!
  (cond
    (should-choose-payment-method? progress)  "/onboarding/security-deposit/payment-method"
    (awaiting-payment-by-check? progress)     "/onboarding/security-deposit/payment-method/check"
    (should-enter-bank-information? progress) "/onboarding/security-deposit/payment-method/ach/verify"
    (should-verify-microdeposits? progress)   "/onboarding/security-deposit/payment-method/ach/microdeposits"
    (should-pay-security-deposit? progress)   "/onboarding/security-deposit/payment-method/ach/pay"
    (security-deposit-finished? progress)     "/onboarding/security-deposit/complete"
    :otherwise                                "/onboarding"))

;; =============================================================================
;; Handlers

(defn- with-gate
  [should-show wrapper]
  (fn [{:keys [identity] :as req}]
    (let [progress (onboarding/get-progress (:db/id identity))]
      (if (should-show progress)
        (ok (wrapper req progress))
        (response/redirect (current-step progress))))))

(defn- update-payment-method
  "Update the payment method if a valid method has been chosen."
  [{:keys [params identity context] :as req}]
  (let [progress                 (onboarding/get-progress (:db/id identity))
        {method :payment-method} params]
    (if-let [method' (get payment-methods method)]
      (do
        (onboarding/update-payment-method progress method')
        (-> (case method
              "check" "/onboarding/security-deposit/payment-method/check"
              "ach"   "/onboarding/security-deposit/payment-method/ach/verify")
            (response/redirect)))
      (respond-with-errors req "Invalid payment method chosen; please try again."
        (view/choose-payment-method (onboarding/payment-method progress))))))

(defn- verify-bank-account
  [{:keys [params identity] :as req}]
  (letfn [(respond-error []
            (respond-with-errors req default-error-message
              (view/enter-bank-information config/public-key)))]
    (if-let [token (:stripe-token params)]
      (try
        (let [customer (stripe/create-customer (:db/id identity) token)]
          (if (stripe/bank-account-verified? customer)
            (response/redirect "/onboarding/security-deposit/payment-method/ach/pay")
            (response/redirect "/onboarding/security-deposit/payment-method/ach/microdeposits")))
        (catch Exception e
          (respond-error)))
      (respond-error))))

(defn- validate-microdeposits
  [params]
  (let [rules [(required "Both deposits are required.")
               [v/integer :message "Please enter the deposit amounts in cents (whole numbers only)."]
               [v/in-range [0 100] :message "Please enter numbers between 1 and 100."]]]
    (b/validate
     params
     {:deposit-1 rules
      :deposit-2 rules})))

(defn verify-microdeposits
  "Verify the microdeposit amounts that were deposited to user's bank account."
  [{:keys [params identity] :as req}]
  (let [params' (transform-when-key-exists params
                  {:deposit-1 str->int :deposit-2 str->int})
        vresult (validate-microdeposits params')]
    (if-let [{:keys [deposit-1 deposit-2]} (valid? vresult)]
      (try
        (stripe/verify-microdeposits (:db/id identity) deposit-1 deposit-2)
        (response/redirect "/onboarding/security-deposit/payment-method/ach/pay")
        (catch Exception e
          (respond-with-errors req (stripe/exception-msg e)
            view/verify-microdeposits)))
      ;; invalid data
      (respond-with-errors req (errors-from vresult)
        view/verify-microdeposits))))

(defn pay-with-ach
  "After the bank account is verified, the user can pay either their full
  deposit or part of it now and the rest at move-in.

  Process the ACH payment."
  [{:keys [params identity] :as req}]
  (let [payment-choice (:payment-choice params)
        progress       (onboarding/get-progress (:db/id identity))]
    (if (#{"full" "partial"} payment-choice)
      (try
        (onboarding/pay-ach progress payment-choice)
        (response/redirect "/onboarding/security-deposit/complete")
        (catch Exception e
          (timbre/error e "Error encountered while attempting to charge user!")
          (respond-with-errors req default-error-message
            (view/pay-by-ach (onboarding/monthly-rent progress)))))
      (respond-with-errors req "Invalid payment choice. Please try again."
        (view/pay-by-ach (onboarding/monthly-rent progress))))))


;; =============================================================================
;; Subroutes

;; /onboarding/security-deposit/payment-method/ach
(defroutes ach-routes
  (GET "/verify" []
       (with-gate should-enter-bank-information?
         (fn [req _]
           (view/enter-bank-information req config/public-key))))

  ;; TODO: Gate POST endpoints
  (POST "/verify" [] verify-bank-account)

  (GET "/microdeposits" []
       (with-gate should-verify-microdeposits?
         (fn [req _]
           (view/verify-microdeposits req))))

  (POST "/microdeposits" [] verify-microdeposits)

  (GET "/pay" [] (with-gate should-pay-security-deposit?
                   (fn [req progress]
                     (view/pay-by-ach req (onboarding/monthly-rent progress)))))

  (POST "/pay" [] pay-with-ach))

;; =============================================================================
;; API
;; =============================================================================

(defroutes routes
  (GET "/" [] (fn [{:keys [identity context] :as req}]
                (view/begin req)))

  (context "/security-deposit" []

    (GET "/" []
         (fn [{:keys [identity]}]
           (response/redirect (-> identity :db/id onboarding/get-progress current-step))))

    (GET "/complete" [] (with-gate security-deposit-finished?
                          (fn [req progress]
                            (let [property (onboarding/applicant-property progress)]
                              (view/security-deposit-complete req (:property/name property))))))

    (context "/payment-method" []
      (GET "/" []
           (with-gate can-choose-payment-method?
             (fn [req progress]
               (view/choose-payment-method req (onboarding/payment-method progress)))))

      (POST "/" [] update-payment-method)

      (GET "/check" [] (with-gate can-make-payment-by-check?
                         (fn [req progress]
                           (view/pay-by-check req (onboarding/monthly-rent progress)))))

      (context "/ach" [] ach-routes))))
