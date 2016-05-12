(ns starcity.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [starcity.routes :as routes]
            [starcity.application.core :as application]
            [reagent.core :as reagent]
            [re-frame.core :refer [register-handler
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe]]))

;; =============================================================================
;; Constants

(enable-console-print!)

;; =============================================================================
;; App-wide handlers

(register-handler
 :app/initialize
 (fn [_ _]
   {:application
    {:personal
     {:basic {:name           {:first "" :last ""}
              :phones         [{:number "" :priority :primary :type :cell}]
              :ssn            ""
              :driver-license {:number "" :state nil}}}}}))

(register-handler
 :app/nav
 (fn [app-state [_ route]]
   (println "Received route dispatch to:" route)
   (assoc-in app-state [:current-route] route)))

;; =============================================================================
;; App-wide Subscriptions

(register-sub
 :app/current-route
 (fn [db _]
   (reaction (:current-route @db))))


;; =============================================================================
;; Entrypoint

(defn main
  []
  [application/main])


(defn ^:export run
  []
  (routes/app-routes)
  (dispatch-sync [:app/initialize])
  (reagent/render [main]
                  (.getElementById js/document "app")))

;; TODO: figure out :figwheel {:on-jsload starcity.core/run}
(run)
