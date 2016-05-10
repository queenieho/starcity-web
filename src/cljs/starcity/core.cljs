(ns starcity.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [starcity.routes :as routes]
            [starcity.application.core :as application]
            [starcity.state :as state]
            [reagent.core :as reagent]
            [re-frame.core :refer [register-handler
                                   path
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe]]))

;; =============================================================================
;; Constants

(enable-console-print!)

(comment
  ;; A "phone" should look like:
  {:priority :primary
   :number   "2345678910" ; string or number? Probably string. We'll never do
                                        ; math on it.
   :type     :cell})


;; =============================================================================
;; Entrypoint

(defn main
  []
  [application/main])

(register-handler
 :initialize
 (fn [db _]
   {:application
    {:personal
     {:basic {:name           {:first "" :last ""}
              :phones         [{:number "" :priority :primary :type :cell}]
              :ssn            ""
              :driver-license {:number "" :state nil}}}}}))

(register-handler
 :app/nav
 (fn [db evt]
   (prn evt)
   db))

(defn ^:export run
  []
  (routes/app-routes)
  (dispatch-sync [:initialize])
  (reagent/render [main]
                  (.getElementById js/document "app")))

;; TODO: figure out :figwheel {:on-jsload starcity.core/run}
(run)
