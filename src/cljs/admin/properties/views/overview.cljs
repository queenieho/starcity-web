(ns admin.properties.views.overview
  (:require [admin.components.content :as c]
            [admin.properties.views.common :refer [metric]]
            [admin.routes :as routes]
            [ant-ui.core :as a]
            [re-frame.core :refer [subscribe]]))

(defn property [property]
  [a/card {:style {:margin-bottom 16}}
   [:div.level
    [:div.level-item
     [:p.title
      [:a {:href (routes/path-for :property :property-id (:db/id property))}
       [:b (:property/name property)]]]]
    (metric "Occupancy"
            (str (:property/total-occupied property) " / " (:property/total-units property)))
    (metric "Rent Due" (str "$" (:property/amount-due property)))
    (metric "Rent Pending" (str "$" (:property/amount-pending property)))
    (metric "Rent Paid" (str "$" (:property/amount-paid property)))
    (metric "Total Rent" (str "$" (:property/amount-total property)))]])

(defn content []
  (let [properties (subscribe [:properties/overview])
        is-loading (subscribe [:properties.overview/fetching?])]
    (fn []
      [c/content
       (if @is-loading
         [a/card {:loading true}]
         [:div
          (map-indexed
           #(with-meta (property %2) {:key %1})
           @properties)])])))
