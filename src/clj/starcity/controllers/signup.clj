(ns starcity.controllers.signup
  (:require [blueprints.models.account :as account]
            [blueprints.models.events :as events]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [clojure.string :refer [capitalize lower-case trim]]
            [customs.auth :as auth]
            [datomic.api :as d]
            [facade.core :as facade]
            [net.cgrand.enlive-html :as html]
            [ring.util.response :as response]
            [starcity.config :as config :refer [config]]
            [starcity.controllers.common :as common]
            [starcity.datomic :refer [conn]]
            [starcity.util.validation :as validation]
            [taoensso.timbre :as timbre]))

;; =============================================================================
;; Constants
;; =============================================================================

(def redirect-after-signup "/signup/complete")

;; =============================================================================
;; Helpers
;; =============================================================================

;; =============================================================================
;; Signup

(defn- validate
  [params]
  (b/validate
   params
   {:email      [[v/required :message "An email address is required."]
                 [v/email :message "That email address is invalid."]]
    :password   [[v/required :message "A password is required."]
                 [v/min-count 8 :message "Your password should be at least 8 characters long."]]
    :first-name [[v/required :message "Please enter your first name."]]
    :last-name  [[v/required :message "Please enter your last name."]]}))

(defn- clean-params
  [params]
  (let [tc (comp trim capitalize)]
    (-> (update params :email (comp trim lower-case))
        (update :password trim)
        (update :first-name tc)
        (update :last-name tc))))

(defn- matching-passwords?
  [{:keys [password-1 password-2] :as params}]
  (when (= password-1 password-2)
    (assoc params :password password-1)))

;; =============================================================================
;; Views
;; =============================================================================

(html/defsnippet signup-complete "templates/signup-complete.html" [:main] [])
(html/defsnippet invalid-activation "templates/invalid-activation.html" [:main] [])

(html/defsnippet signup-main "templates/signup.html" [:main]
  [& {:keys [errors form]}]
  [:div.alerts] (facade/maybe-errors errors)
  [:#first-name] (html/set-attr :value (:first-name form))
  [:#last-name] (html/set-attr :value (:last-name form))
  [:#email] (html/set-attr :value (:email form)))

(defn- signup-view
  [req & {:keys [errors form]}]
  (common/page req {:main        (signup-main :errors errors :form form)
                    :css-bundles ["public.css"]
                    :js-bundles  ["main.js"]
                    :header      (common/header :signup)}))

;; =============================================================================
;; Handlers
;; =============================================================================

;; =============================================================================
;; Signup

(defn- show-invalid-activation [req]
  (->> (common/page req {:css-bundles ["public.css"]
                         :js-bundles  ["main.js"]
                         :main        (invalid-activation)})
       (common/render-ok)))

(defn show-complete [req]
  (->> (common/page req {:css-bundles ["public.css"]
                         :js-bundles  ["main.js"]
                         :main        (signup-complete)})
       (common/render-ok)))

(defn- signup-errors [req form & errors]
  (->> (signup-view req :form form :errors errors) (common/render-malformed)))

(defn show-signup
  "Show the signup page."
  [req]
  (->> (signup-view req) (common/render-ok)))

(defn signup
  [{:keys [params] :as req}]
  (if-let [params (matching-passwords? params)]
    (let [vresult (-> params clean-params validate)]
      (if-let [{:keys [email password first-name last-name]} (validation/valid? vresult)]
        (if-not (account/exists? (d/db conn) email)
          (do
            @(d/transact-async conn [(events/create-account email (auth/hash-password password) first-name last-name)])
            (response/redirect redirect-after-signup))
          ;; account already exists for email
          (signup-errors req params (format "An account is already registered for %s." email)))
        ;; validation failure
        (apply signup-errors req params (validation/errors vresult))))
    ;; passwords don't match
    (signup-errors req params "Those passwords do not match. Please try again.")))

;; =============================================================================
;; Activation

(defn activate
  [{:keys [params session] :as req}]
  (let [{:keys [email hash]} params]
    (if (or (nil? email) (nil? hash))
      (show-invalid-activation req)
      (let [acct (account/by-email (d/db conn) email)]
        (if (= hash (account/activation-hash acct))
          (let [session (assoc session :identity (auth/session-data acct))]
            (do
              @(d/transact conn [(auth/activate acct)])
              (timbre/info :account/activated {:email email})
              (-> (response/redirect (config/apply-hostname config))
                  (assoc :session session))))
          ;; hashes don't match
          (show-invalid-activation req))))))
