(ns starcity.controllers.settings
  (:require [starcity.controllers.utils :refer [ok errors-from valid?]]
            [starcity.web.messages :refer [respond-with-errors
                                           respond-with-success]]
            [starcity.views.settings :as view]
            [clojure.string :as str]
            [starcity.util :refer :all]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [starcity.models.account :as account]
            [ring.util.response :as response]
            [ring.util.codec :refer [url-encode]]))

;; =============================================================================
;; Validation
;; =============================================================================

(defn- validate-password-params
  [params account-id]
  (letfn [(matching-password? [password]
            (account/is-password? account-id password))]
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

(def show-account-settings
  (comp ok view/account-settings))

(defn update-password!
  [{:keys [identity params] :as req}]
  (if-let [params (-> params scrub-password-params matching-passwords?)]
    (let [vresult (validate-password-params params (:db/id identity))]
      (if-let [{:keys [password]} (valid? vresult)]
        (do
          (account/change-password! (:db/id identity) password)
          (respond-with-success req "Successfully changed your password!" view/account-settings))
        (respond-with-errors req (errors-from vresult) view/account-settings)))
    (respond-with-errors req "Your passwords must match!" view/account-settings)))
