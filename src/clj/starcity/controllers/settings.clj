(ns starcity.controllers.settings
  (:require [bouncer
             [core :as b]
             [validators :as v]]
            [clojure.string :as str]
            [starcity.models.account :as account]
            [starcity.util :refer :all]))

;; =============================================================================
;; Validation
;; =============================================================================

(defn- validate-password-params
  [params account]
  (letfn [(matching-password? [password]
            (account/is-password? account password))]
    (b/validate
     params
     {:current-password [[v/required :message "You must enter your current password."]
                         [matching-password? :message "That is not the correct current password."]]
      :password         [[v/required :message "You must enter a new password."]
                         [v/min-count 8 :message "Your password should be at least 8 characters long."]]})))

(defn- scrub-password-params
  [params]
  (transform-when-key-exists params
    {:password-1 str/trim
     :password-2 str/trim}))

(defn- matching-passwords?
  [{:keys [password-1 password-2] :as params}]
  (when (= password-1 password-2)
    (assoc params :password password-1)))

;; =============================================================================
;; API
;; =============================================================================

#_(def show-account-settings
  (comp ok view/account-settings))

#_(defn update-password
  [{:keys [params] :as req}]
  (if-let [params (-> params scrub-password-params matching-passwords?)]
    (let [account (auth/requester req)
          vresult (validate-password-params params account)]
      (if-let [{:keys [password]} (valid? vresult)]
        (do
          @(d/transact conn [(account/change-password account password)])
          (respond-with-success req "Successfully changed your password!" view/account-settings))
        (respond-with-errors req (errors-from vresult) view/account-settings)))
    (respond-with-errors req "Your passwords must match!" view/account-settings)))
