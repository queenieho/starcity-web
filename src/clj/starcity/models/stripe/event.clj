(ns starcity.models.stripe.event
  (:require [datomic.api :as d]
            [starcity.datomic :refer [conn tempid]]
            [starcity.services.stripe :as service]
            [starcity.services.stripe.event :as event]
            [starcity.spec]
            [clojure.spec :as s]))

(s/def ::status #{:stripe-event.status/failed
                  :stripe-event.status/succeeded
                  :stripe-event.status/processing})

(defn failed?
  "Did the event fail?"
  [event]
  (= (:stripe-event/status event) :stripe-event.status/failed))

(defn succeeded?
  "Did the event succeed?"
  [event]
  (= (:stripe-event/status event) :stripe-event.status/succeeded))

(defn status
  "Set the event's status to `new-status`."
  [new-status event]
  (d/transact conn [{:db/id               (:db/id event)
                     :stripe-event/status new-status}]))

;; TODO: rename to `set-status`?
(s/fdef status
        :args (s/cat :new-status ::status
                     :event :starcity.spec/entity))

(def processing
  (partial status :stripe-event.status/processing))

(def failed
  (partial status :stripe-event.status/failed))

(def succeeded
  (partial status :stripe-event.status/succeeded))

(defn lookup
  "Look up an event by `event-id` from the db."
  [event-id]
  (d/entity (d/db conn) [:stripe-event/event-id event-id]))

(defn create
  "Create a new Stripe event with status `processing`."
  [event-id event-type]
  (let [tid (tempid)
        tx  @(d/transact conn [{:db/id                 tid
                                :stripe-event/event-id event-id
                                :stripe-event/status   :stripe-event.status/processing
                                :stripe-event/type     event-type}])]
    (d/entity (d/db conn) (d/resolve-tempid (d/db conn) (:tempids tx) tid))))

(defn fetch
  "Attempt to fetch event from Stripe identified by `event-id`. Throws an
  exception on failure."
  [event-id]
  (let [res (event/fetch event-id)]
    (if-let [e (service/error-from res)]
      (throw (ex-info "Error encountered while trying to fetch event." e))
      (service/payload-from res))))

(comment

  (fetch "")

  (d/touch (create "blah" "something"))

  )
