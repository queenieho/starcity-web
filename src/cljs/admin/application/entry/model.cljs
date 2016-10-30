(ns admin.application.entry.model
  (:require [admin.application.entry.db :refer [root-db-key]]
            [starcity.utils :refer [find-by]]
            [starcity.log :as l]))

(defn application [db]
  (let [application-id (get-in db [root-db-key :current-id])]
    (get-in db [root-db-key :applications application-id])))

(defn initial-deposit-amount [application community]
  (->> (:properties application)
       (find-by (comp #{community} :property/internal-name))
       :base-price))
