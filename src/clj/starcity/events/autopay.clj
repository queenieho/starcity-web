(ns starcity.events.autopay
  (:require [datomic.api :as d]
            [starcity.datomic :refer [conn]]
            [starcity.events.plumbing :refer [defproducer]]
            [starcity.models.member-license :as member-license]))

(defproducer unsubscribe! ::unsubscribe
  [subscription-id]
  (let [license (member-license/by-subscription-id conn subscription-id)]
    {:result  @(d/transact conn [(member-license/remove-subscription license)])
     :license license}))
