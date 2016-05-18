(ns starcity.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [starcity.routes :as routes]
            [starcity.phases.one.core :as phase-one]
            [starcity.phases.common :as phase]
            [reagent.core :as reagent]
            [re-frame.core :refer [register-handler
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe]]))

;; =============================================================================
;; Helpers

(enable-console-print!)

;; =============================================================================
;; Handlers

(register-handler
 :app/initialize
 (fn [_ _]
   {:phases    [{:id      2
                 :start   {:section "basic" :group "name"}
                 :enabled true
                 :title   "Rental Application"}]
    :basic     {:name           {:first "" :last ""}
                :phones         [{:priority :primary :type :cell}]
                :ssn            ""
                :driver-license {:number "" :state nil}}
    :residence {}}))

(register-handler
 :app/nav
 (fn [app-state [_ route params]]
   (assoc app-state :location {:current route :params params})))

(register-handler
 ::debug
 (fn [app-state _]
   (.log js/console app-state)
   app-state))

;; =============================================================================
;; Subscriptions

(register-sub
 :app/current-location
 (fn [db _]
   (reaction (get-in @db [:location :current]))))

(register-sub
 ::phases
 (fn [db _]
   (reaction (mapv (fn [{:keys [id title start enabled]}]
                     {:params  (merge {:phase-id id} start)
                      :title   title
                      :enabled enabled})
                   (:phases @db)))))

;; =============================================================================
;; Entrypoint

(defn phase-link [params content]
  [:a {:href (routes/phase params)} content])

(defn dashboard-content []
  (let [phases (subscribe [::phases])]
    (fn []
      [:div
       [:div.page-header [:h1 "Welcome!"]]
       [:ul
        (for [{:keys [params title enabled]} @phases]
          ^{:key title} [:li (if enabled
                               [phase-link params title]
                               title)])]])))

(defn main
  []
  (let [location (subscribe [:app/current-location])]
    (fn []
      [:div.container
       (case @location
         :phase/two [phase-one/main]    ; TODO: naming is off
         [dashboard-content])
       [:div.row
        [:div.col-lg-12
         [:button.btn.btn-default {:on-click #(dispatch [::debug])}
          "app-state"]]]])))


(defn ^:export run
  []
  (routes/app-routes)
  (dispatch-sync [:app/initialize])
  (reagent/render [main] (.getElementById js/document "app")))

;; TODO: figure out :figwheel {:on-jsload starcity.core/run}
(run)
