(ns starcity.models.property
  (:require [starcity.datomic :refer [conn]]
            [starcity.config :as config]
            [starcity.models.util :refer [mapify]]
            [datomic.api :as d]
            [starcity.config :as config]))

;; =============================================================================
;; API
;; =============================================================================

(defn create! [name internal-name units-available]
  (let [entity (mapify :property
                       {:name            name
                        :internal-name   internal-name
                        :units-available units-available})
        tid    (d/tempid (config/datomic-partition))
        tx     @(d/transact conn [(assoc entity :db/id tid)])]
    (d/resolve-tempid (d/db conn) (:tempids tx) tid)))
