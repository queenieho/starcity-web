(ns starcity.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [register-handler
                                   path
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe]]))

(enable-console-print!)

(defn main
  []
  [:div [:h1 "Hello from re-frame!!"]])

(defn ^:export run
  []
  ;; (dispatch-sync [:initialize])
  (reagent/render [main]
                  (.getElementById js/document "app")))

;; TODO: figure out :figwheel {:on-jsload starcity.core/run}
(run)
