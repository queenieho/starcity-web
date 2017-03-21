(ns starcity.controllers.common
  (:require [ring.util.response :as response]))

(defn ok
  [body]
  (-> (response/response body)
      (response/content-type "text/html")))

(defn malformed
  [body]
  (-> (response/response body)
      (response/status 400)
      (response/content-type "text/html")))
