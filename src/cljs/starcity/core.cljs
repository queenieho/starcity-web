(ns starcity.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [starcity.routes :as routes]
            [starcity.sections.basic :as basic]
            [starcity.sections.residence :as residence]
            [reagent.core :as reagent]
            [re-frame.core :refer [register-handler
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe]]))

;; =============================================================================
;; Helpers

(enable-console-print!)

(defn make-sections
  [sections]
  {:forward  (zipmap sections (rest sections))
   :backward (zipmap (rest sections) sections)})

(defn current->next
  [sections current]
  (get-in sections [:forward current]))

(defn current->previous
  [sections current]
  (get-in sections [:backward current]))

;; =============================================================================
;; Handlers

(def SECTIONS
  [:basic/name :basic/ssn :basic/phones :basic/drivers-license
   :residence/street-address])

(register-handler
 :app/initialize
 (fn [_ _]
   {:progress :basic/name
    :sections (make-sections SECTIONS)
    :basic    {:name           {:first "" :last ""}
               :phones         [{:priority :primary :type :cell}]
               :ssn            ""
               :driver-license {:number "" :state nil}}
    :residence {}}))

(register-handler
 :app/nav
 (fn [app-state [_ route]]
   (println "Received route dispatch to:" route)
   (assoc-in app-state [:current-route] route)))

(register-handler
 :progress/next!
 (fn [app-state [_ current]]
   (let [sections (get app-state :sections)]
     (assoc-in app-state [:progress] (current->next sections current)))))

(register-handler
 :progress/previous!
 (fn [app-state [_ current]]
   (let [sections (get app-state :sections)]
     (assoc-in app-state [:progress] (current->previous sections current)))))

(register-handler
 ::debug
 (fn [app-state _]
   (.log js/console app-state)
   app-state))

;; =============================================================================
;; Subscriptions

(register-sub
 :app/current-route
 (fn [db _]
   (reaction (:current-route @db))))

(register-sub
 :progress/section
 (fn [db _]
   (reaction (-> @db :progress namespace keyword))))

(register-sub
 :progress/group
 (fn [db _]
   (reaction (-> @db :progress name keyword))))

;; =============================================================================
;; Entrypoint


(defn main
  []
  (let [current-section (subscribe [:progress/section])]
    (fn []
      [:div.container
       (case @current-section
         :basic     [basic/form]
         :residence [residence/form]
         [:p "Unrecognized Section"])
       [:button.btn.btn-default {:on-click #(dispatch [::debug])}
        "app-state"]])))


(defn ^:export run
  []
  (routes/app-routes)
  (dispatch-sync [:app/initialize])
  (reagent/render [main]
                  (.getElementById js/document "app")))

;; TODO: figure out :figwheel {:on-jsload starcity.core/run}
(run)
