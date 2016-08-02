(ns starcity.middleware
  (:require [starcity.views.error :as view]
            [taoensso.timbre :refer [infof error]]))

(def ^:private params-blacklist
  #{:password :password-1 :password-2})

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
      (infof "REQUEST :: uri -- %s :: params -- %s :: method -- %s"
              uri (apply dissoc params params-blacklist) request-method))
    (handler req)))
