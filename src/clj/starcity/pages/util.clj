(ns starcity.pages.util
  (:require [ring.util.response :refer [response]]))


(defn html-response
  [response]
  (assoc response :headers {"Content-Type" "text/html; charset=utf-8"}))

(def ok (comp html-response response))
