(ns starcity.controllers.login
  (:require [blueprints.models.account :as account]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [clojure.string :refer [lower-case trim]]
            [customs.auth :as auth]
            [datomic.api :as d]
            [facade.core :as facade]
            [net.cgrand.enlive-html :as html]
            [ring.util.response :as response]
            [starcity.config :as config :refer [config]]
            [starcity.controllers.common :as common]
            [starcity.datomic :refer [conn]]
            [starcity.util.validation :as validation]))

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
    (account/applicant? account)  (config/apply-hostname config)
    (account/onboarding? account) "/onboarding"
    :otherwise                    (config/odin-hostname config)))


;; =============================================================================
;; Views
;; =============================================================================


(html/defsnippet login-main "templates/login.html" [:main]
  [errors]
  [:div.alerts] (facade/maybe-errors errors))


;; =============================================================================
;; Handlers
;; =============================================================================


(defn- view
  [req & errors]
  (common/page req {:css-bundles ["public.css"]
                    :js-bundles  ["main.js"]
                    :main        (login-main errors)}))


(defn show
  "Show the login page."
  [req]
  (common/render-ok (view req)))


(defn login
  "Log a user in."
  [{:keys [params session] :as req}]
  (let [vresult (-> params clean-credentials validate-credentials)]
    (if-let [{:keys [email password]} (validation/valid? vresult)]
      (if-let [account (auth/authenticate (d/db conn) email password)]
        (if (:account/activated account)
          ;; success
          (let [next-url (url-after-login account req)
                session  (assoc session :identity account)]
            (-> (response/redirect next-url)
                (assoc :session session)))
          ;; account not activated
          (common/render-malformed (view req unactivated-error)))
        ;; authentication failure
        (common/render-malformed (view req invalid-credentials-error)))
      ;; validation failure
      (common/render-malformed (apply view req (validation/errors vresult))))))
