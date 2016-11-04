(ns admin.application.list.views
  (:require [admin.routes :refer [build-path]]
            [admin.components.table :as tbl]
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

(defn- table []
  (let [header-data (subscribe [:application.list/header])]
    (fn []
      [:table.table
       [tbl/header @header-data :application.list/sort header-titles]
       [table-body (:keys @header-data)]])))

;; =============================================================================
;; API
;; =============================================================================

(defn applications []
  [:div.container
   [title]
   [table]])
