(ns starcity.middleware
  (:require [ring.util.response :as response]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [buddy.auth.backends.session :refer [session-backend]]
            [taoensso.timbre :refer [tracef error]]))

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
      (tracef "REQUEST :: uri -- %s :: params -- %s :: method -- %s"
              uri params request-method))
    (handler req)))

(defn wrap-components
  "Middleware to inject components into the request map."
  [handler & kvs]
  (assert (even? (count kvs)))
  (let [components (partition 2 kvs)]
    (fn [req]
      (handler (reduce (fn [acc [k c]]
                         (assoc-in acc [::components k] c))
                       req
                       components)))))

(defn get-component [req k]
  (get-in req [::components k]))

(defn wrap-environment
  "Inject a keyword identifying the current run environment into requests."
  [handler env]
  (fn [req]
    (handler (assoc req ::environment env))))

(defn get-environment [req]
  (get req ::environment))

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
