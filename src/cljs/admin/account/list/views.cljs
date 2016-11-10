(ns admin.account.list.views
  (:require [admin.routes :refer [build-path]]
            [admin.components.table :as tbl]
            [re-frame.core :refer [subscribe dispatch]]
            [starcity.dates :as d]
            [cljs-time.coerce :as c]
            [starcity.dom :as dom]
            [starcity.components.loading :as loading]
            [reagent.core :as r]))

;; =============================================================================
;; Internal
;; =============================================================================

(def ^:private format-date
  (comp (partial d/format :short-date) c/to-local-date))

(def ^:private header-titles
  {:phone-number "phone number"
   :created-at   "created at"})

(def ^:private transforms
  "Transform value under header by applying these functions."
  {:created-at format-date})

(defn- value-cell [k account]
  (let [tf (get transforms k identity)]
    [:td (tf (get account k))]))

(defn- link-cell [k account]
  (let [link    (str "/admin/accounts/" (get account :id))
        content (get account k)]
    [:td [:a {:href link} content]]))

(def ^:private is-link-cell? #{:name})

(defn- table-row [header-keys account]
  [:tr
   (for [k header-keys]
     (if (is-link-cell? k)
       ^{:key k} [link-cell k account]
       ^{:key k} [value-cell k account]))])

(defn- table-body [header-keys]
  (let [accounts (subscribe [:account.list/list])]
    (fn [header-keys]
      [:tbody
       (map-indexed
        (fn [idx a]
          ^{:key (:id a)} [table-row header-keys (assoc a :number (inc idx))])
        @accounts)])))

(defn- pagination []
  (let [num-pages    (subscribe [:account.list.pagination/num-pages])
        current-page (subscribe [:account.list.pagination/page-num])
        has-previous (subscribe [:account.list.pagination/has-previous?])
        has-next     (subscribe [:account.list.pagination/has-next?])]
    (fn []
      [:nav.pagination
       [:a.button
        {:class    (when-not @has-previous "is-disabled")
         :on-click #(dispatch [:account.list.pagination/previous])}
        "Previous"]
       [:a.button
        {:class    (when-not @has-next "is-disabled")
         :on-click #(dispatch [:account.list.pagination/next])}
        "Next"]
       [:ul
        (doall
         (for [i (range @num-pages)]
           ^{:key (str "page-" i)}
           [:li [:button.button
                 {:class    (when (= @current-page i) "is-primary")
                  :on-click #(dispatch [:account.list.pagination/goto-page i])}
                 (inc i)]]))]])))

(defn table []
  (let [header-data (subscribe [:account.list/header])
        is-loading  (subscribe [:account.list/loading?])]
    (fn []
      (if @is-loading
        (loading/container)
        [:div
         [:table.table
          [tbl/header @header-data :account.list/sort header-titles]
          [table-body (:keys @header-data)]]
         [pagination]]))))

(defn title []
  (let [current-view    (subscribe [:account.list.view/current])
        available-views (subscribe [:account.list.view/available])
        query           (subscribe [:account.list/query])]
    (r/create-class
     {:component-did-mount
      (fn [_]
        (.focus (js/document.getElementById "search")))
      :reagent-render
      (fn []
        [:div
         [:h3.title.is-3 "Accounts"]
         [:div.columns
          [:div.column.is-two-thirds
           [:label.label "Search"]
           [:p.control.has-icon
            [:input#search.input {:type        "text"
                                  :value       @query
                                  :placeholder "Search by name or email"
                                  :on-change   #(dispatch [:account.list.query/change (dom/val %)])}]
            [:i.fa.fa-search]]]
          [:div.column
           [:div.is-pulled-right
            [:label.label.has-text-right "Currently Viewing"]
            [:span.select
             [:select {:value @current-view :on-change #(dispatch [:account.list.view/change (dom/val %)])}
              (for [view @available-views]
                ^{:key view} [:option {:value view} (name view)])]]]]]])})))

;; =============================================================================
;; API
;; =============================================================================

(defn accounts []
  [:div.container
   [title]
   [table]])
