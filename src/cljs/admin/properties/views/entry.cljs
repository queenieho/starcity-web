(ns admin.properties.views.entry
  (:require [admin.components.content :as c]
            [admin.components.table :as tbl]
            [admin.properties.views.common :refer [metric]]
            [admin.routes :as routes]
            [ant-ui.core :as a]
            [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [cljs-time.format :as f]
            [cljs-time.coerce :as coerce]))

(defn- overview [property]
  [a/card
   [:div.level
    [:div.level-item
     [:p.title (:property/name property)]]
    (metric "Occupancy"
            (str (:property/total-occupied property) " / " (:property/total-units property)))
    (metric "Rent Due" (str "$" (:property/amount-due property)))
    (metric "Rent Pending" (str "$" (:property/amount-pending property)))
    (metric "Rent Paid" (str "$" (:property/amount-paid property)))
    (metric "Total Rent" (str "$" (:property/amount-total property)))
    (metric "Connect" [:a {:target "_blank" :href (:property/stripe-url property)} "Stripe"])]])

(defn- unit-columns [property-id]
  [(tbl/column "name"
               :title "Unit Name"
               :render
               (fn [name record]
                 (r/as-element [:a {:href (routes/path-for :unit
                                                           :property-id property-id
                                                           :unit-id (:id record))}
                                name])))
   (tbl/column "account"
               :title "Member"
               :render
               (fn [account record]
                 (if account
                   (r/as-element
                    [:a {:href (routes/path-for :account :account-id (:id account))}
                     (:name account)])
                   "N/A")))

   (tbl/column "rate" :render #(if % (str "$" %) "N/A"))
   (tbl/column "term" :title "Term (months)" :render #(if % % "N/A"))])

(defn- units [property]
  [a/card {:title "Units"}
   [a/table {:dataSource (clj->js (:property/units property))
             :columns    (unit-columns (:db/id property))
             :size       :small}]])

(def ^:private date-formatter (f/formatter "yyyy-MM-dd"))

(defn- available-on [property]
  [:div.variable
   [:label.label "Available On"]
   [:p.variable__help-text
    "Controls the availability date displayed on the website and application."]
   (when-let [available-on (:property/available-on property)]
     [:input.variable__control.input
      {:style        {:width 200}
       :type         "date"
       :defaultValue (f/unparse date-formatter (coerce/to-date-time available-on))
       :onChange     #(dispatch [:property.update/available-on (:db/id property)
                                 (.. % -target -value)])}])])

(defn- ops-fee [property]
  [:div.variable
   [:label.label "Operations Fee"]
   [:p.variable__help-text
    "Controls the percentage that will be taken by Starcity Ops during rent payments."]
   [a/input-number
    {:class         "variable__control"
     :min           0
     :max           100
     :default-value (or (:property/ops-fee property) 0)
     :onChange      #(dispatch [:property.update/ops-fee (:db/id property) %])}]])

(defn- license [property-id license-price]
  [:div.control.is-expanded
   [:label.label (str (:license-price/term license-price) " Month")]
   [a/input-number
    {:min           0
     :step          50
     :default-value (or (:license-price/price license-price) 0)
     :onChange      #(dispatch [:property.update/license property-id
                                (assoc license-price :license-price/price %)])}]])

(defn- licenses [property]
  [:div.variable
   [:label.label "Licenses"]
   [:p.variable__help-text
    "These values represent the price of rent at this building, "
    [:em "except"]
    " for units that have their own prices set. At the very least, they serve to tell"
    " applicants roughly how much they'll be paying in rent."]
   [:div.field.is-grouped
    (map-indexed
     #(with-meta (license (:db/id property) %2) {:key %1})
     (:property/licenses property))]])

(defn- variables [property]
  [a/card {:title "Variables"}
   [:div
    [available-on property]
    [ops-fee property]
    [licenses property]]])

(defn content []
  (let [property   (subscribe [:property/viewing])
        is-loading (subscribe [:property.viewing/fetching?])]
    (fn []
      [c/content
       (if @is-loading
         [a/card {:loading true}]
         [:div
          [overview @property]
          [:div.columns {:style {:margin-top 8}}
           [:div.column.is-half
            [units @property]]
           [:div.column
            [variables @property]]]])])))
