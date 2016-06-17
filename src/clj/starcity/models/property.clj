(ns starcity.models.property
  (:require [starcity.datomic :refer [conn]]
            [starcity.config :as config]
            [starcity.models.util :refer [mapify]]
            [datomic.api :as d]
            [starcity.config :as config]))

;; =============================================================================
;; API
;; =============================================================================

(defn available-leases [property]
  (->> (d/q '[:find ?ls
              :in $ ?p
              :where [?p :property/available-leases ?ls]]
            (d/db conn) (:db/id property))
       (map first)))

(defn units [property]
  (->> (d/q '[:find ?units
              :in $ ?p
              :where
              [?p :property/units ?units]
              [?units :unit/available-on _]]
            (d/db conn) (:db/id property))
       (map first)))

(defn create! [name internal-name units-available]
  (let [entity (mapify :property
                       {:name            name
                        :internal-name   internal-name
                        :units-available units-available})
        tid    (d/tempid (config/datomic-partition))
        tx     @(d/transact conn [(assoc entity :db/id tid)])]
    (d/resolve-tempid (d/db conn) (:tempids tx) tid)))
