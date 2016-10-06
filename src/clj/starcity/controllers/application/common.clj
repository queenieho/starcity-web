(ns starcity.controllers.application.common
  (:require [ring.util.response :as response]
            [buddy.auth.accessrules :as auth]
            [starcity.views.error :as view]
            [starcity.models.application :as application]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- passes [predicate]
  (fn [req]
    (if (predicate req)
      (auth/success)
      (auth/error))))

;; (defn- on-error
;;   [req error-key]
;;   (if (= error-key ::locked)
;;     (response/redirect "/application")
;;     (let [error-message (message-for-key error-key)]
;;       (-> (view/error error-message)
;;           (response/response)
;;           (response/content-type "text/html; charset=utf-8")
;;           (response/status 403)))))

;; =============================================================================
;; API
;; =============================================================================

(defn not-locked
  [{:keys [identity] :as req}]
  (let [account-id (:db/id identity)]
    (if (application/locked-old? account-id)
      (auth/error ::locked)
      (auth/success))))

(defn on-error
  [_ _]
  (response/redirect "/application"))

(defn restrictions
  [predicate]
  {:handler  {:and [(passes predicate) not-locked]}
   :on-error on-error})
