(ns starcity.api.common
  (:require [ring.util.response :refer [response]]))

;; =============================================================================
;; API
;; =============================================================================

(defn account-id
  "Return the `account-id` of the user that initiated this request."
  [req]
  (get-in req [:identity :db/id]))

(def json "application/json; charset=utf-8")
(def transit "application/transit+json")

(defn json-response
  [response]
  (assoc response :headers {"Content-Type" "application/json; charset=utf-8"}))

(def ok (comp json-response response))

(defn malformed [body]
  (-> (response body)
      (json-response)
      (assoc :status 400)))

(defn unprocessable [body]
  (-> (response body)
      (json-response)
      (assoc :status 422)))

(defn server-error [& errors]
  (let [default-error "Our bad! Something went wrong on our end. Please try again."]
    (-> (response {:errors (if (empty? errors) [default-error] errors)})
        (json-response)
        (assoc :status 500))))

(defn ok? [response]
  (= (:status response) 200))
