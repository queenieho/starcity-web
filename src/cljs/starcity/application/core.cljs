(ns starcity.application.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [starcity.components.form :as form]
            [starcity.application.basic :as basic]
            [reagent.core :as reagent]
            [re-frame.core :refer [register-handler
                                   register-sub
                                   dispatch
                                   subscribe]]))
;; =============================================================================
;; API

(defn main
  []
  [:div.container
   [:h1 "Starcity Rental Application"]
   [basic/form]])
