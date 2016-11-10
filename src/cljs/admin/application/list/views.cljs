(ns admin.application.list.views
  (:require [admin.routes :refer [build-path]]
            [admin.components.table :as tbl]
            [re-frame.core :refer [subscribe dispatch]]
            [starcity.dates :as d]
            [starcity.log :refer [log]]
            [clojure.string :as str]
            [cljs-time.coerce :as c]
            [starcity.dom :as dom]
            [starcity.components.loading :as loading]
            [starcity.components.icons :as i]
            [reagent.core :as r]))

;; =============================================================================
;; Helpers
;; =============================================================================

(def ^:private format-date
  (comp (partial d/format :short-date) c/to-local-date))

;; =============================================================================
;; Components
;; =============================================================================

(def ^:private transforms
  "Transform value under header by applying these functions."
  {:move-in      format-date
   :completed-at format-date
   :properties   (partial str/join ", ")})

(defn- value-cell [k application]
  (let [tf (get transforms k identity)]
    [:td (tf (get application k))]))

(defn- link-cell [k application]
  (let [link    (str "/admin/applications/" (get application :id))
        content (get application k)]
    [:td [:a {:href link} content]]))

(def ^:private is-link-cell? #{:name})

(defn- table-row [header-keys application]
  [:tr
   (for [k header-keys]
     (if (is-link-cell? k)
       ^{:key k} [link-cell k application]
       ^{:key k} [value-cell k application]))])

(defn- table-body [header-keys]
  (let [applications (subscribe [:application.list/list])]
    (fn [header-keys]
      [:tbody
       (map-indexed
        (fn [idx a]
          ^{:key (:id a)} [table-row header-keys (assoc a :number (inc idx))])
        @applications)])))

(def ^:private header-titles
  {:phone-number "phone number"
   :move-in      "desired move-in"
   :completed-at "completed at"})

(defn- pagination []
  (let [num-pages    (subscribe [:application.list.pagination/num-pages])
        current-page (subscribe [:application.list.pagination/page-num])
        has-previous (subscribe [:application.list.pagination/has-previous?])
        has-next     (subscribe [:application.list.pagination/has-next?])]
    (fn []
      [:nav.pagination
       [:a.button
        {:class    (when-not @has-previous "is-disabled")
         :on-click #(dispatch [:application.list.pagination/previous])}
        "Previous"]
       [:a.button
        {:class    (when-not @has-next "is-disabled")
         :on-click #(dispatch [:application.list.pagination/next])}
        "Next"]
       [:ul
        (doall
         (for [i (range @num-pages)]
           ^{:key (str "page-" i)}
           [:li [:button.button
                 {:class    (when (= @current-page i) "is-primary")
                  :on-click #(dispatch [:application.list.pagination/goto-page i])}
                 (inc i)]]))]])))

(defn- table []
  (let [header-data (subscribe [:application.list/header])
        is-loading  (subscribe [:application.list/loading?])]
    (fn []
      (if @is-loading
        (loading/container)
        [:div
         [:table.table
          [tbl/header @header-data :application.list/sort header-titles]
          [table-body (:keys @header-data)]]
         [pagination]]))))

(defn- title []
  (let [current-view    (subscribe [:application.list.view/current])
        available-views (subscribe [:application.list.view/available])
        query           (subscribe [:application.list/query])]
    (r/create-class
     {:component-did-mount
      (fn [_]
        (.focus (js/document.getElementById "search")))
      :reagent-render
      (fn []
        [:div
         [:h3.subtitle.is-3 "Applications"]
         [:div.columns
          [:div.column.is-two-thirds
           [:label.label "Search"]
           [:p.control.has-icon
            [:input#search.input {:type        "text"
                                  :value       @query
                                  :placeholder "Search by name, email or community"
                                  :on-change   #(dispatch [:application.list.query/change (dom/val %)])}]
            [:i.fa.fa-search]]]
          [:div.column
           [:div.is-pulled-right
            [:label.label.has-text-right "Currently Viewing"]
            [:span.select
             [:select {:value @current-view :on-change #(dispatch [:application.list.view/change (dom/val %)])}
              (for [view @available-views]
                ^{:key view} [:option {:value view} (name view)])]]]]]])})))

;; =============================================================================
;; API
;; =============================================================================

(defn applications []
  [:div.container
   [title]
   [:br]
   [table]])
