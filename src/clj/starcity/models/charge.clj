(ns starcity.models.charge
  (:require [datomic.api :as d]
            [starcity.datomic :refer [conn]]
            [starcity.spec]
            [clojure.spec :as s]))

(defn- status-tx [status charge]
  [{:db/id         (:db/id charge)
    :charge/status status}])

(def succeeded-tx
  "The transaction to mark `charge` as succeeded."
  (partial status-tx :charge.status/succeeded))

(def failed-tx
  "The transaction to mark `charge` as failed."
  (partial status-tx :charge.status/failed))

(defn succeeded
  "Mark `charge` as succeeded."
  [charge]
  (d/transact conn (succeeded-tx charge)))

(defn failed
  "Mark `charge` as failed."
  [charge]
  (d/transact conn (failed-tx charge)))

(defn lookup
  "Look up a charge by the external `charge-id`."
  [charge-id]
  (d/entity (d/db conn) [:charge/stripe-id charge-id]))

(defn- is-status? [status charge]
  (= (:charge/status charge) status))

(def is-succeeded? (partial is-status? :charge.status/succeeded))
(def is-failed? (partial is-status? :charge.status/failed))
(def is-pending? (partial is-status? :charge.status/pending))
