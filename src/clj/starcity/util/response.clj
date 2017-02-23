(ns starcity.util.response
  (:require [ring.util.response :as resp]))

(defn malformed
  "Given a response `body`, produce a resposne with status code 400."
  [body]
  (-> (resp/response body)
      (resp/status 400)))

(defn unprocessable
  "Given a response `body`, produce a resposne with status code 422."
  [body]
  (-> (resp/response body)
      (resp/status 422)))

(defn ok
  "Given a response `body`, produce a resposne with status code 200."
  [body]
  (resp/response body))

(defn json [response]
  (resp/content-type response "application/json; charset=utf-8"))

(defn transit [response]
  (resp/content-type response "application/transit+json"))

(def transit-malformed (comp transit malformed))
(def json-malformed (comp json malformed))

(def transit-unprocessable (comp transit unprocessable))
(def json-unprocessable (comp json unprocessable))

(def json-ok (comp json ok))
(def transit-ok (comp transit ok))
