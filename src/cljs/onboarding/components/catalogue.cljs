(ns onboarding.components.catalogue
  (:require [re-frame.core :refer [dispatch]]
            [starcity.components.service :as service]))

(defn- row
  [items grid-size opts]
  [:div.columns
   (map-indexed
    #(with-meta [:div.column [service/card %2 opts]] {:key %1})
    items)])

(defn- inject-selected
  [orders items]
  (let [selected (keys orders)]
    (reduce
     (fn [acc {service :service :as item}]
       (if ((set selected) service)
         (conj acc (assoc item :selected (get orders service)))
         (conj acc item)))
     []
     items)))

(defn grid
  "Render a `catalogue` of services as a grid."
  [keypath catalogue orders & {:keys [grid-size] :or {grid-size 2}}]
  (let [items (->> (inject-selected orders (:items catalogue))
                   (partition grid-size grid-size []))
        opts  {:on-change #(dispatch [:prompt.orders/update keypath %])
               :on-select (fn [{:keys [service fields] :as item}]
                            (dispatch [:prompt.orders/select keypath item]))
               :on-delete (fn [{service :service}]
                            (dispatch [:prompt.orders/remove keypath service]))}]
    [:div
     (map-indexed
      #(with-meta (row %2 grid-size opts) {:key %1})
      items)]))
