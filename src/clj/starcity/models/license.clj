(ns starcity.models.license
  (:require [starcity.models.util :refer :all]
            [datomic.api :as d]))

(defn licenses [conn]
  (let [db (d/db conn)]
    (->> (d/q '[:find [?e ...]
                :where
                [?e :license/term _]
                (or [?e :license/available true]
                    [(missing? $ ?e :license/available)])]
              db)
         (map (partial d/entity db)))))
