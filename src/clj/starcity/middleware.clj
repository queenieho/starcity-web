(ns starcity.middleware
  (:require [starcity.views.error :as view]
            [starcity.log :as log]))

(defn wrap-exception-handling
  [handler]
  (fn [req]
    (try
      (handler req)
      (catch Exception e
        (do
          (log/request req e)
          {:status 500 :body (view/error "Unexpected server error.")})))))

(defn wrap-logging
  "Middleware to log requests."
  [handler]
  (fn [{:keys [uri params request-method identity] :as req}]
    (when-not (= uri "/favicon.ico")
      (log/request req))
    (handler req)))
