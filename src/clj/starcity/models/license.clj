(ns starcity.models.license
  (:require [starcity.models.util :refer :all]
            [datomic.api :as d]))

(def term :license/term)

(defn licenses [conn]
  (let [db (d/db conn)]
    (->> (d/q '[:find [?e ...]
                :where
                [?e :license/term _]
                (or [?e :license/available true]
                    [(missing? $ ?e :license/available)])]
              db)
         (map (partial d/entity db)))))

(defn by-term [conn term]
  (->> (d/q '[:find ?e .
              :in $ ?t
              :where
              [?e :license/term ?t]]
            (d/db conn) term)
       (d/entity (d/db conn))))
