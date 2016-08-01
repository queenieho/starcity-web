(ns starcity.api.common
  (:require [ring.util.response :refer [response]]))

;; =============================================================================
;; API
;; =============================================================================

(defn json-response
  [response]
  (assoc response :headers {"Content-Type" "application/json; charset=utf-8"}))

(def ok (comp json-response response))

(defn malformed [body]
  (-> (response body)
      (json-response)
      (assoc :status 400)))

(defn ok? [response]
  (= (:status response) 200))
