(ns starcity.services.common
  (:require [cheshire.core :as json]))

(defn parse-json-body
  "Parse the :body in the response as JSON."
  [res]
  (update-in res [:body] json/parse-string true))
