(ns admin.accounts.views.entry.license
  (:require [admin.routes :as routes]
            [ant-ui.core :as a]
            [clojure.string :as string]
            [re-frame.core :refer [subscribe]]
            [reagent.core :as r]
            [toolbelt.date :as date]))

(def columns
  [{:title     "Status"
    :dataIndex "status"
    :key       "status"
    :render    (fnil (comp string/lower-case name) "")}
   {:title     "Starts"
    :dataIndex "starts"
    :key       "starts"
    :render    date/short-date}
   {:title     "Ends"
    :dataIndex "ends"
    :key       "ends"
    :render    date/short-date}
   {:title     "Term"
    :dataIndex "term"
    :key       "term"}
   {:title     "Rate"
    :dataIndex "rate"
    :key       "rate"
    :render    #(str "$" %)}
   {:title     "Property"
    :dataIndex "property"
    :key       "property"
    :render    (fn [[id name]]
                 (let [link (routes/path-for :property :property-id id)]
                   (r/as-element [:a {:href link} name])))}
   {:title     "Unit"
    :dataIndex "unit"
    :key       "unit"
    :render    (fn [[id name] record]
                 (let [record (js->clj record :keywordize-keys true)
                       link   (routes/path-for :unit
                                               :property-id (-> record :property first)
                                               :unit-id id)]
                   (r/as-element [:a {:href link} name])))}])

(defn- license->row [license]
  (let [unit     (:license/unit license)
        property (:license/property license)]
    {:key      (:db/id license)
     :status   (:license/status license)
     :starts   (:license/starts license)
     :ends     (:license/ends license)
     :rate     (:license/rate license)
     :term     (:license/term license)
     :unit     ((juxt :db/id :unit/name) unit)
     :property ((juxt :db/id :property/name) property)}))

(defn licenses
  "Component that displays the current account's licenses in tabular format."
  []
  (let [licenses (subscribe [:account/licenses])]
    (fn []
      [a/table {:dataSource (clj->js (map license->row @licenses))
                :columns    columns
                :size       :small}])))
