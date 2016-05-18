(ns starcity.phases.one.sections.residence
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent :refer [atom]]
            [reforms.reagent :include-macros true :as f]
            [reforms.validation :include-macros true :as v]
            [re-frame.core :refer [register-handler
                                   register-sub
                                   dispatch
                                   subscribe]]))

(defn form []
  [:div
   [:h1 "Residence History"]
   [:p "TODO: This is where the residence stuff will go."]])
