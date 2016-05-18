(ns starcity.phases.one.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [starcity.routes :as routes]
            [starcity.phases.one.sections.basic :as basic]
            [starcity.phases.one.sections.residence :as residence]
            [starcity.phases.common :as phase]
            [reagent.core :as reagent]
            [re-frame.core :refer [register-handler
                                   register-sub
                                   dispatch
                                   subscribe]]))

;; =============================================================================
;; Constants

;; =============================================================================
;; API

(defn main []
  (let [current-section (subscribe [:phase/current-section])]
    (fn []
      [:div.container
       (case @current-section
         :basic     [basic/form]
         :residence [residence/form]
         [:p "Unrecognized Section"])
       ])))
