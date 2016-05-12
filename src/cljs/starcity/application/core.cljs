(ns starcity.application.core
  (:require [starcity.application.basic :as basic]
            [starcity.application.residence :as residence]
            [starcity.routes :as routes]
            [re-frame.core :refer [subscribe]]))

;; =============================================================================
;; Helpers

(defn- title-for [route]
  (case route
    :basic "Basic Information"
    :residence "Residence History"
    "Choose Section"))

;; =============================================================================
;; Components

(defn- content []
  (let [sections [[(routes/basic) "Basic Information"]
                  [(routes/residence) "Residence History"]]]
    [:ul
     (for [[route desc] sections]
       ^{:key route} [:li [:a {:href route} desc]])]))

;; =============================================================================
;; API

(defn main
  []
  (let [route (subscribe [:app/current-route])]
    (fn []
      [:div.container
       [:h1 (title-for @route)]
       (case @route
         :basic     [basic/form]
         :residence [residence/form]
         [content])])))
