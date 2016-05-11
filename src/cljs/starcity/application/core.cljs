(ns starcity.application.core
  (:require [starcity.application.basic :as basic]))

;; =============================================================================
;; API

(defn main
  []
  [:div.container
   [:h1 "Starcity Rental Application"]
   [basic/form]])
