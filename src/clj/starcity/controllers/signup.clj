(ns starcity.controllers.signup
  (:require [bouncer
             [core :as b]
             [validators :as v]]
            [clojure.string :refer [capitalize lower-case trim]]
            [datomic.api :as d]
            [net.cgrand.enlive-html :as html]
            [ring.util.response :as response]
            [starcity.controllers
             [common :as common]
             [utils :refer :all]]
            [starcity.datomic :refer [conn]]
            [starcity.models
             [account :as account]
             [cmd :as cmd]]
            [starcity.views.base :as base]
            [taoensso.timbre :as timbre]))

;; =============================================================================
;; Constants
;; =============================================================================

(def redirect-after-signup "/signup/complete")
(def redirect-after-activation "/apply")

;; =============================================================================
;; Helpers
;; =============================================================================

;; =============================================================================
;; Signup

(defn- validate
  [params]
  (b/validate
   params
   {:email      [(required "An email address is required.")
                 [v/email :message "That email address is invalid."]]
    :password   [(required "A password is required.")
                 [v/min-count 8 :message "Your password should be at least 8 characters long."]]
    :first-name [(required "Please enter your first name.")]
    :last-name  [(required "Please enter your last name.")]}))

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
  [:div.alerts] (base/maybe-errors errors)
  [:#first-name] (html/set-attr :value (:first-name form))
  [:#last-name] (html/set-attr :value (:last-name form))
  [:#email] (html/set-attr :value (:email form)))

(defn- signup-view
  [req & {:keys [errors form]}]
  (base/public-base req
                    :main (signup-main :errors errors :form form)
                    :header (base/header :signup)))

;; =============================================================================
;; Handlers
;; =============================================================================

;; =============================================================================
;; Signup

(defn- show-invalid-activation [req]
  (->> (base/public-base req :main (invalid-activation))
       (common/render-ok)))

(defn show-complete [req]
  (->> (base/public-base req :main (signup-complete))
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
      (if-let [{:keys [email password first-name last-name]} (valid? vresult)]
        (if-not (account/exists? (d/db conn) email)
          (do
            @(d/transact conn [(cmd/create-account email password first-name last-name)])
            (response/redirect redirect-after-signup))
          ;; account already exists for email
          (signup-errors req params (format "An account is already registered for %s." email)))
        ;; validation failure
        (apply signup-errors req params (errors-from vresult))))
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
          (let [session (assoc session :identity (account/session-data acct))]
            (do
              @(d/transact conn [(account/activate acct)])
              (timbre/info :account/activated {:email email})
              (-> (response/redirect redirect-after-activation)
                  (assoc :session session))))
          ;; hashes don't match
          (show-invalid-activation req))))))
