(ns starcity.controllers.login
  (:require [bouncer
             [core :as b]
             [validators :as v]]
            [buddy.auth :refer [authenticated?]]
            [clojure.string :refer [lower-case trim]]
            [ring.util.response :as response]
            [starcity.controllers.utils :refer :all]
            [starcity.models.account :as account]
            [starcity.views.login :as view]
            [starcity.web.messages :refer [respond-with-errors]]))

;; =============================================================================
;; Constants
;; =============================================================================

(def redirect-after-login "/me")

(def unactivated-error
  "Please click the activation link in your inbox before attempting to log in.")

(def invalid-credentials-error
  "The credentials you entered are invalid; please try again.")

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- validate-credentials
  [credentials]
  (b/validate
   credentials
   {:email    [[v/required :message "An email address is required."]
               [v/email :message "That email address is invalid."]]
    :password [[v/required :message "A password is required."]
               [v/min-count 8 :message "Your password should be at least 8 characters long."]]}))

(defn- clean-credentials
  [credentials]
  (-> (update credentials :email (comp trim lower-case))
      (update :password trim)))

(defn- url-after-login [acct {:keys [params] :as req}]
  (cond
    (not-empty (:next params)) (:next params)
    (account/admin? acct)      "/admin"
    (account/applicant? acct)  "/apply"
    :otherwise                 redirect-after-login))

;; =============================================================================
;; API
;; =============================================================================

(defn show-login
  "Respond 200 OK with the login page."
  [req]
  (if (authenticated? req)
    (response/redirect "/apply")
    (ok (view/login req))))

(defn login
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
          (respond-with-errors req unactivated-error view/login))
        ;; authentication failure
        (respond-with-errors req invalid-credentials-error view/login))
      ;; validation failure
      (respond-with-errors req (errors-from vresult) view/login))))
