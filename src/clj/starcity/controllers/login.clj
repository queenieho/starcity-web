(ns starcity.controllers.login
  (:require [bouncer
             [core :as b]
             [validators :as v]]
            [clojure.string :refer [lower-case trim]]
            [datomic.api :as d]
            [ring.util.response :as response]
            [selmer.parser :as selmer]
            [starcity.controllers
             [common :as common]
             [utils :refer :all]]
            [starcity.datomic :refer [conn]]
            [starcity.models.account :as account]
            [starcity.views.common :refer [public-defaults]]))

;; =============================================================================
;; Constants
;; =============================================================================

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

(defn- url-after-login
  [account {:keys [params] :as req}]
  (cond
    (not (empty? (:next params))) (:next params)
    (account/admin? account)      "/admin"
    (account/applicant? account)  "/apply"
    (account/onboarding? account) "/onboarding"
    :otherwise                    "/me"))

;; =============================================================================
;; Handlers
;; =============================================================================

(defn- render
  [req & errors]
  (selmer/render-file "login.html" (assoc (public-defaults req) :errors errors)))

(defn show
  "Show the login page."
  [req]
  (common/ok (render req)))

(defn login
  "Log a user in."
  [{:keys [params session] :as req}]
  (let [vresult (-> params clean-credentials validate-credentials)]
    (if-let [{:keys [email password]} (valid? vresult)]
      (if-let [account (account/authenticate (d/db conn) email password)]
        (if (:account/activated account)
          ;; success
          (let [next-url (url-after-login account req)
                session  (assoc session :identity account)]
            (-> (response/redirect next-url)
                (assoc :session session)))
          ;; account not activated
          (common/malformed (render req unactivated-error)))
        ;; authentication failure
        (common/malformed (render req invalid-credentials-error)))
      ;; validation failure
      (common/malformed (apply render req (errors-from vresult))))))
