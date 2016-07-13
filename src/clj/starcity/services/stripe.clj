(ns starcity.services.stripe
  (:require [org.httpkit.client :as http]
            [starcity.config :refer [config]]
            [mount.core :refer [defstate]]
            [cheshire.core :as json]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- charge-request
  [{:keys [secret-key] :as config}]
  (fn [amount token email & {:keys [description cb] :or {description ""}}]
    (-> @(http/post "https://api.stripe.com/v1/charges"
                    {:form-params {:amount      amount
                                   :source      token
                                   :currency    "usd"
                                   :description description}
                     :basic-auth  [secret-key ""]
                     :headers     {"Content-Type" "application/x-www-form-urlencoded"}})
        (update-in [:body] json/parse-string true))))

;; =============================================================================
;; API
;; =============================================================================

(defstate charge :start (charge-request (:stripe config)))
