(ns starcity.controllers.auth.login
  (:require [starcity.views.base :refer [base]]
            [starcity.views.auth.login :as view]
            [starcity.models.account :as account]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [clojure.string :refer [trim lower-case]]
            [starcity.controllers.utils :refer :all]
            [ring.util.response :as response]))

;; =============================================================================
;; Constants
;; =============================================================================

(def +redirect-after-login+ "/me")

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- validate-credentials
  [credentials]
  (b/validate
   credentials
   {:email    [(required "An email address is required.")
               [v/email :message "That email address is invalid."]]
    :password [(required "A password is required.")
               [v/min-count 8 :message "Your password should be at least 8 characters long."]]}))

(defn- clean-credentials
  [credentials]
  (-> (update credentials :email (comp trim lower-case))
      (update :password trim)))

(defn- url-after-login [acct {:keys [params] :as req}]
  (cond
    (not-empty (:next params)) (:next params)
    (account/applicant? acct)  "/application"
    :otherwise                 +redirect-after-login+))

(defn- show* [{:keys [identity] :as req}
              & {:keys [errors email] :or {errors [] email ""}}]
  ;; NOTE: Preserves the next url through the POST req by using a hidden input
  (let [next-url (get-in req [:params :next])]
    (base (view/login errors email next-url) :css ["login.css"])))

;; =============================================================================
;; API
;; =============================================================================

(defn show-login
  "Show the login page."
  [req]
  (ok (show* req)))

(defn login!
  "Log a user in."
  [{:keys [params session] :as req}]
  (let [vresult (-> params clean-credentials validate-credentials)]
    (if-let [{:keys [email password]} (valid? vresult)]
      (if-let [acct (account/authenticate email password)]
        (if (:account/activated acct)
          ;; success
          (let [next-url (url-after-login acct req)
                session  (assoc session :identity acct)]
            (-> (response/redirect next-url)
                (assoc :session session)))
          ;; account not activated
          (malformed (show* req
                            :errors ["Please click the activation link in your inbox before attempting to log in."]
                            :email email)))
        ;; authentication failure
        (malformed (show* req
                          :errors ["The credentials you entered are invalid; please try again."]
                          :email email)))
      ;; validation failure
      (malformed (show* req
                        :errors (errors-from vresult)
                        :email (:email params))))))
