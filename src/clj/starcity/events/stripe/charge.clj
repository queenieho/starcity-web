(ns starcity.events.stripe.charge
  (:require [clojure.core.async :as async :refer [go]]
            [clojure.spec :as s]
            [dire.core :refer [with-pre-hook!]]
            [starcity.datomic :refer [conn]]
            [starcity.events.stripe.charge
             [failed :as failed]
             [succeeded :as succeeded]]
            [starcity.events.util :as e :refer [chan?]]
            [starcity.models.charge :as charge]
            [taoensso.timbre :as timbre]))

(defn- ->key [base charge-type]
  (keyword (namespace base) (str (name base) "." (name charge-type))))

(defn succeeded!
  "A Stripe charge has succeeded."
  [charge-id amount]
  (go
    (try
      (let [charge (charge/lookup charge-id)
            res    (succeeded/update-db conn charge amount)]
        (succeeded/notify-internal conn charge)
        (succeeded/notify-user conn charge)
        res)
      (catch Throwable ex
        (timbre/error ex ::succeeded {:charge-id charge-id
                                      :amount    amount})
        ex))))

(with-pre-hook! #'succeeded!
  (fn [c a] (timbre/info ::succeeded {:charge-id c :amount a})))

(s/fdef succeeded!
        :args (s/cat :charge-id string? :amount integer?)
        :ret chan?)

(defn failed!
  "A Stripe charge has failed."
  [charge-id failure-message]
  (go
    (try
      (let [charge (charge/lookup charge-id)
            res    (failed/update-db conn charge)]
        (failed/notify-internal conn charge)
        (failed/notify-user conn charge)
        res)
      (catch Throwable ex
        (timbre/error ex ::failed {:charge-id       charge-id
                                   :failure-message failure-message})
        ex))))

(with-pre-hook! #'failed!
  (fn [c msg] (timbre/info ::failed {:charge-id c :failure-message msg})))

(s/fdef failed!
        :args (s/cat :charge-id string? :message string?)
        :ret chan?)
