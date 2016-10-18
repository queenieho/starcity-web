(ns admin.application.list.views
  (:require [admin.routes :refer [build-path]]
            [re-frame.core :refer [subscribe dispatch]]
            [starcity.dates :as d]
            [starcity.log :refer [log]]
            [clojure.string :as str]
            [cljs-time.coerce :as c]))

;; =============================================================================
;; Helpers
;; =============================================================================

(def ^:private format-date
  (comp (partial d/format :short-date) c/to-local-date))

;; =============================================================================
;; Components
;; =============================================================================

(defn- title []
  [:h1.title.is-1 "Applications"])

(def ^:private headers
  {:phone-number "phone number"
   :move-in      "desired move-in"
   :completed-at "completed at"})

(def ^:private header-attrs
  (reduce
   (fn [acc k]
     (assoc acc k {:on-click #(dispatch [:applications.list/sort k])}))
   {}
   [:term :move-in :completed-at]))

;; The below logic seems a little convoluted
;; One idea: [:term :asc] OR :none
(defn- inject-sort-classes
  [{:keys [key direction]} attrs header-key]
  (let [active? (and (not= :none direction)
                     (= header-key key))
        init    (if (contains? header-attrs header-key)
                  ["is-sortable"]
                  [])
        classes (cond-> init
                  active?                   (conj "is-active")
                  (and active?
                       (= :desc direction)) (conj "is-descending")
                  (and active?
                       (= :asc direction))  (conj "is-ascending"))]
    (assoc attrs :class (str/join " " classes))))

(defn table-header [keys]
  (let [sort (subscribe [:application.list/sort])]
    (fn [keys]
      [:thead
       [:tr
        (doall
         (for [k keys]
           (let [attrs   (get header-attrs k {})
                 content (get headers k (name k))]
             ^{:key k} [:th
                        (inject-sort-classes @sort attrs k)
                        content])))]])))

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

(def ^:private is-link-cell?
  #{:name})

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

(defn- table []
  (let [keys (subscribe [:application.list/header-keys])]
    (fn []
      [:table.table
       [table-header @keys]
       [table-body @keys]])))

;; =============================================================================
;; API
;; =============================================================================

(defn applications []
  [:div.container
   [title]
   [table]])
