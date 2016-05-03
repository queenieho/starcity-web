(ns starcity.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [starcity.routes :as routes]
            [reagent.core :as reagent]
            [re-frame.core :refer [register-handler
                                   path
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe]]
            [re-com.core :as com]))

;; =============================================================================
;; Constants

(enable-console-print!)

(def app-db (reagent/atom {}))

;; =============================================================================
;; input-text

(defn handle-input-text
  [app-state [_ text]]
  (assoc-in app-state [:input-text] text))

(register-handler :input-text/changed handle-input-text)

(register-sub
 :input-text
 (fn [db _]
   (reaction (get-in @db [:input-text]))))

(defn input-text
  []
  (let [text (subscribe [:input-text])]
    (fn []
      [com/input-text
       :model @text
       :on-change #(dispatch [:input-text/changed %])])))

;; =============================================================================
;; Entrypoint

(defn main
  []
  [:div.container
   [:h1 "Rental Application from Reagent"]])

(register-handler
 :initialize
 (fn [db _]
   {:input-text "Hello, world!"}))

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
