(ns starcity.application.residence
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [starcity.components.form :as form]
            [starcity.util :refer [event-value]]
            [reagent.core :as reagent]
            [re-frame.core :refer [register-handler
                                   register-sub
                                   dispatch
                                   subscribe]]))

;; =============================================================================
;; API

(defn form
  []
  [:form {:on-submit #(do
                        (dispatch [:residence/submit])
                        (.preventDefault %))}
   ])
