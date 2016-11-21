(ns starcity.datomic.schema.stripe-event
  (:require [datomic-schema.schema :as s]
            [starcity.datomic.partition :refer [tempid]]))

(def ^:private statuses
  [{:db/id    (tempid)
    :db/ident :stripe-event.status/processing}
   {:db/id    (tempid)
    :db/ident :stripe-event.status/succeeded}
   {:db/id    (tempid)
    :db/ident :stripe-event.status/failed}])

(def ^{:added "1.1.0"} schema
  (->> (s/generate-schema
        [(s/schema
          stripe-event
          (s/fields
           [event-id :string :unique-identity
            "The Stripe ID for this event."]
           [type :string
            "The event's type."]
           [status :ref
            "The status of this event. One of #{:processing :succeeded :failed}"]))])
       (concat statuses)))

(def norms
  {:schema/add-stripe-event-schema-11-1-16
   {:txes     [schema]
    :requires [:starcity/add-starcity-partition]}})
