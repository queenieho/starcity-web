(ns starcity.middleware
  (:require [starcity.views.error :as view]
            [taoensso.timbre :refer [debugf error]]))

(defn wrap-exception-handling
  [handler]
  (fn [req]
    (try
      (handler req)
      (catch Exception e
        (do
          (error e)
          {:status 500 :body (view/error "Unexpected server error.")})))))

(defn wrap-logging
  "Middleware to log requests."
  [handler]
  (fn [{:keys [uri params request-method] :as req}]
    (when-not (= uri "/favicon.ico")
      (debugf "REQUEST :: uri -- %s :: params -- %s :: method -- %s"
              uri params request-method))
    (handler req)))
