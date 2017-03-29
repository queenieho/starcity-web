(ns starcity.controllers.common
  (:require [ring.util.response :as response]))

(defn ok
  [body]
  (-> (response/response body)
      (response/content-type "text/html")))

(defn render [t]
  (apply str t))

(def render-ok
  (comp ok render))

(defn malformed
  [body]
  (-> (response/response body)
      (response/status 400)
      (response/content-type "text/html")))

(def render-malformed
  (comp malformed render))
