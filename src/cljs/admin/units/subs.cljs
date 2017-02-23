(ns admin.units.subs
  (:require [admin.units.db :as db]
            [re-frame.core :refer [reg-sub]]))

(reg-sub
 ::units
 (fn [db _]
   (db/path db)))

(reg-sub
 :units
 :<- [::units]
 (fn [db _]
   (:units db)))

(reg-sub
 :unit/viewing-id
 :<- [::units]
 (fn [db _]
   (:viewing db)))

(reg-sub
 :unit/viewing
 :<- [:unit/viewing-id]
 :<- [::units]
 (fn [[unit-id {units :units}] _]
   (get units unit-id)))

(reg-sub
 :unit.viewing/fetching?
 :<- [::units]
 (fn [db _]
   (db/fetching-unit? db)))

(reg-sub
 :unit.viewing/name
 :<- [:unit/viewing]
 (fn [unit _]
   (:unit/name unit)))

(defn- row [licenses]
  (let [grouped (group-by :source licenses)]
    (reduce
     (fn [row [source [data]]]
       (case source
         :unit     (assoc row
                          :id (:db/id data)
                          :term (:license-price/term data)
                          :unit-price (:license-price/price data))
         :property (assoc row
                          :term (:license-price/term data)
                          :property-price (:license-price/price data))))
     {}
     grouped)))

(defn- license-prices->data-source [unit-prices property-prices]
  (let [us (map #(assoc % :source :unit) unit-prices)
        ps (map #(assoc % :source :property) property-prices)]
    (->> (concat us ps)
         (group-by :license-price/term)
         (reduce
          (fn [acc [_ licenses]]
            (conj acc (row licenses)))
          [])
         (sort-by :term))))

(reg-sub
 :unit.viewing/license-prices
 :<- [:unit/viewing]
 (fn [unit _]
   (let [unit-prices (:unit/licenses unit)
         prop-prices (:property/licenses unit)]
      (license-prices->data-source unit-prices prop-prices))))
