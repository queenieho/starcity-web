(ns starcity.middleware
  (:require [ring.util.response :as response]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [buddy.auth.backends.session :refer [session-backend]]
            [taoensso.timbre :refer [tracef error debugf]]))

(defn wrap-exception-handling
  [handler]
  (fn [req]
    (try
      (handler req)
      (catch Exception e
        (do
          (error "ERROR:" e)
          (println e)
          {:status 500 :body "Unexpected server error."})))))

(defn wrap-logging
  "Middleware to log requests."
  [handler]
  (fn [{:keys [uri params request-method] :as req}]
    (when-not (= uri "/favicon.ico")
      (debugf "REQUEST :: uri -- %s :: params -- %s :: method -- %s"
              uri params request-method))
    (handler req)))

(defn- unauthorized-handler
  [request metadata]
  (cond
    (authenticated? request) (-> (response/response "UNAUTHENTICATED")
                                 (response/content-type "text/html; charset=utf-8")
                                 (assoc :status 403))
    :else                    (let [current-url (:uri request)]
                               (response/redirect (format "/login?next=%s" current-url)))))

(def auth-backend
  (session-backend {:unauthorized-handler unauthorized-handler}))
