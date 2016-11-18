(ns starcity.controllers.onboarding
  (:require [bouncer
             [core :as b]
             [validators :as v]]
            [compojure.core :refer [context defroutes GET POST]]
            [ring.util.response :as response]
            [starcity
             [auth :as auth]
             [log :as log]
             [util :refer :all]]
            [starcity.controllers.utils :refer [errors-from ok valid?]]
            [starcity.models
             [account :as account]
             [onboarding :as onboarding]
             [stripe :as stripe]]
            [starcity.views.onboarding :as view]
            [starcity.web.messages :as msg :refer [respond-with-errors]]))

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
  "User should enter bank information iff ach is chosen and no Stripe customer
  is created yet, OR ach is chosen and verification has failed for the existing
  stripe customer."
  [progress]
  (and (ach? progress)
       (or (not (onboarding/customer-created? progress))
           (onboarding/verification-failed? progress))))

(defn- should-verify-microdeposits?
  "User should verify bank account iff chosen payment method is ACH and his/her
  bank account is not yet verified."
  [progress]
  (and (ach? progress)
       (onboarding/customer-created? progress)
       (not (onboarding/verification-failed? progress))
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

(defn- current-step-url
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
        (response/redirect (current-step-url progress))))))

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
      (respond-with-errors
       (assoc req :payment-method (onboarding/payment-method progress))
       "Invalid payment method chosen; please try again."
       view/payment-method))))

(defn- verify-bank-account
  [{:keys [params] :as req}]
  (let [account (auth/requester req)
        err-res (respond-with-errors req default-error-message view/enter-bank-information)]
    (if-let [token (:stripe-token params)]
      (try
        (let [{:keys [customer entity]} (stripe/create-customer! account token)]
          (log/info ::create-customer {:token       token
                                       :user        (account/email account)
                                       :customer-id (:id customer)
                                       :entity-id   (:db/id entity)})
          (if (stripe/bank-account-verified? customer)
            (response/redirect "/onboarding/security-deposit/payment-method/ach/pay")
            (response/redirect "/onboarding/security-deposit/payment-method/ach/microdeposits")))
        (catch Exception e
          (log/exception e ::create-customer {:token token
                                              :user  (account/email account)})
          err-res))
      err-res)))

(defn- validate-microdeposits
  [params]
  (let [rules [[v/required :message "Both deposits are required."]
               [v/integer :message "Please enter the deposit amounts in cents (whole numbers only)."]
               [v/in-range [0 100] :message "Please enter numbers between 1 and 100."]]]
    (b/validate
     params
     {:deposit-1 rules
      :deposit-2 rules})))

(defn verify-microdeposits
  "Verify the microdeposit amounts that were deposited to user's bank account."
  [{:keys [params] :as req}]
  (let [account (auth/requester req)
        params' (transform-when-key-exists params
                  {:deposit-1 str->int :deposit-2 str->int})
        vresult (validate-microdeposits params')]
    (if-let [{:keys [deposit-1 deposit-2]} (valid? vresult)]
      (try
        (let [res (stripe/verify-microdeposits account deposit-1 deposit-2)]
          (log/info ::verify-microdeposits {:user        (account/email account)
                                            :customer-id (:customer res)}))
        (response/redirect "/onboarding/security-deposit/payment-method/ach/pay")
        (catch Exception e
          (log/exception e ::verify-microdeposits {:user (account/email account)})
          (respond-with-errors req (stripe/exception-msg e)
                               view/verify-microdeposits)))
      ;; invalid data
      (respond-with-errors req (errors-from vresult)
                           view/verify-microdeposits))))

(defn pay-with-ach
  "After the bank account is verified, the user can pay either their full
  deposit or part of it now and the rest at move-in.

  Process the ACH payment."
  [{:keys [params] :as req}]
  (let [account        (auth/requester req)
        payment-choice (:payment-choice params)
        progress       (onboarding/get-progress (:db/id account))]
    (letfn [(-respond-error [msg]
              (let [full-amt (onboarding/full-deposit-amount progress)
                    code     (onboarding/property-code progress)]
                (respond-with-errors
                 (assoc req :full-deposit-amount full-amt :property-code code)
                 msg
                 view/pay-by-ach)))]
      (if (#{"full" "partial"} payment-choice)
        (try
          (onboarding/pay-ach progress payment-choice)
          (log/info ::ach-payment {:customer-id         (onboarding/stripe-customer-id progress)
                                   :user                (account/email account)
                                   :payment-choice      payment-choice
                                   :security-deposit-id (onboarding/security-deposit-id progress)})
          (response/redirect "/onboarding/security-deposit/complete")
          (catch Exception e
            (log/exception e ::ach-payment {:customer-id         (onboarding/stripe-customer-id progress)
                                            :user                (account/email account)
                                            :payment-choice      payment-choice
                                            :security-deposit-id (onboarding/security-deposit-id progress)})
            (-respond-error default-error-message)))
        (-respond-error "Invalid payment choice. Please try again.")))))


;; =============================================================================
;; Subroutes

;; /onboarding/security-deposit/payment-method/ach
(defroutes ach-routes
  (GET "/verify" []
       (with-gate should-enter-bank-information?
         (fn [req _]
           (view/enter-bank-information req))))

  (POST "/verify" [] verify-bank-account)

  (GET "/microdeposits" []
       (with-gate should-verify-microdeposits?
         (fn [req _]
           (view/verify-microdeposits req))))

  (POST "/microdeposits" [] verify-microdeposits)

  (GET "/pay" []
       (with-gate should-pay-security-deposit?
         (fn [req progress]
           (let [full-amt (onboarding/full-deposit-amount progress)
                 code     (onboarding/property-code progress)]
             (-> (assoc req
                        :full-deposit-amount full-amt
                        :property-code code)
                 view/pay-by-ach)))))

  (POST "/pay" [] pay-with-ach))

;; =============================================================================
;; API
;; =============================================================================

(defroutes routes
  (GET "/" [] (fn [{:keys [context identity] :as req}]
                (let [progress       (onboarding/get-progress (:db/id identity))
                      ideal-step-url (current-step-url progress)]
                  (if (= ideal-step-url context)
                    (ok (view/begin req))
                    (response/redirect ideal-step-url)))))

  (context "/security-deposit" []

           (GET "/" []
                (fn [{:keys [identity]}]
                  (response/redirect (-> identity :db/id onboarding/get-progress current-step-url))))

           (GET "/complete" []
                (with-gate security-deposit-finished?
                  (fn [req progress]
                    (let [property (onboarding/applicant-property progress)]
                      (-> (assoc req :property-name (:property/name property))
                          (view/security-deposit-complete))))))

           (context "/payment-method" []
                    (GET "/" []
                         (with-gate can-choose-payment-method?
                           (fn [req progress]
                             (->> (onboarding/payment-method progress)
                                  (assoc req :payment-method)
                                  (view/payment-method)))))

                    (POST "/" [] update-payment-method)

                    (GET "/check" []
                         (with-gate can-make-payment-by-check?
                           (fn [req progress]
                             (let [full-amt      (onboarding/full-deposit-amount progress)
                                   property-code (onboarding/property-code progress)]
                               (-> (assoc req
                                          :full-deposit-amount full-amt
                                          :property-code property-code)
                                   (view/pay-by-check))))))

                    (context "/ach" [] ach-routes))))
