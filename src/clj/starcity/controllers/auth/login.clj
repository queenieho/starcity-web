(ns starcity.controllers.auth.login
  (:require [starcity.views.auth.login :as view]
            [starcity.models.account :as account]
            [buddy.auth :refer [authenticated?]]
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

(defn- show-login*
  "NOTE: Preserves the next url through the POST req by using a hidden input."
  [{:keys [identity params] :as req} & {:keys [errors email] :or {errors []}}]
  (let [next-url (get-in req [:params :next])
        email    (or email (:email params) "")]
    (view/login errors email next-url (-> (get-in req [:params :activated]) nil? not))))

;; =============================================================================
;; API
;; =============================================================================

(defn show-login
  "Respond 200 OK with the login page."
  [req]
  (if (authenticated? req)
    (response/redirect "/application")
    (ok (show-login* req))))

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
          (malformed
           (show-login* req
                        :errors ["Please click the activation link in your inbox before attempting to log in."]
                        :email email)))
        ;; authentication failure
        (malformed
         (show-login* req
                      :errors ["The credentials you entered are invalid; please try again."]
                      :email email)))
      ;; validation failure
      (malformed
       (show-login* req
                    :errors (errors-from vresult)
                    :email (:email params))))))
