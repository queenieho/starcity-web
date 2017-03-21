(ns starcity.controllers.signup
  (:require [bouncer
             [core :as b]
             [validators :as v]]
            [clojure.string :refer [capitalize lower-case trim]]
            [datomic.api :as d]
            [plumbing.core :as plumbing]
            [ring.util.response :as response]
            [selmer.parser :as selmer]
            [starcity.controllers
             [common :as common]
             [utils :refer :all]]
            [starcity.datomic :refer [conn]]
            [starcity.models
             [account :as account]
             [cmd :as cmd]]
            [starcity.views.common :refer [public-defaults]]
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
;; Handlers
;; =============================================================================

;; =============================================================================
;; Signup

(defn- show-invalid-activation
  [req]
  (common/ok (selmer/render-file "invalid-activation.html" (public-defaults req))))

(defn show-complete
  [req]
  (common/ok (selmer/render-file "signup-complete.html" (public-defaults req))))

(defn- signup-errors
  [req {:keys [first-name last-name email]} & errors]
  (-> (selmer/render-file "signup.html" (-> (public-defaults req)
                                            (assoc :errors errors)
                                            (plumbing/assoc-when :first-name first-name
                                                                 :last-name last-name
                                                                 :email email)))
      (common/malformed)))

(defn show-signup
  "Show the signup page."
  [req]
  (common/ok (selmer/render-file "signup.html" (public-defaults req))))

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
