(ns starcity.datomic.schema
  (:require [starcity.datomic.conformity :as c]
            [starcity.datomic.schema
             [account :as account]
             [address :as address]
             [approval :as approval]
             [charge :as charge]
             [community-safety :as community-safety]
             [license :as license]
             [member-application :as member-application]
             [member-license :as member-license]
             [partition :as partition]
             [plaid :as plaid]
             [property :as property]
             [income-file :as income-file]
             [security-deposit :as security-deposit]
             [session :as session]
             [stripe-customer :as stripe-customer]
             [stripe-event :as stripe-event]]
            [starcity.log :as log]))

(defn assemble-norms []
  (merge account/norms
         address/norms
         approval/norms
         charge/norms
         community-safety/norms
         license/norms
         member-application/norms
         member-license/norms
         partition/norms
         plaid/norms
         property/norms
         income-file/norms
         security-deposit/norms
         session/norms
         stripe-customer/norms
         stripe-event/norms))

(defn install [conn]
  (let [norms (assemble-norms)]
    ;; TODO: check out `c/speculative-conn` and `c/with-conforms` to think of a
    ;; way to do this more safely and/or with better logging
    (c/ensure-conforms conn norms)))
