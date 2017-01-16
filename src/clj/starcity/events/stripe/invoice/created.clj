(ns starcity.events.stripe.invoice.created
  (:require [datomic.api :as d]
            [starcity.models
             [account :as account]
             [member-license :as member-license]
             [rent-payment :as rent-payment]]))

(defn- customer-id->member-license [conn id]
  (->> (d/q '[:find ?ml .
              :in $ ?sc
              :where
              [?sc :stripe-customer/account ?a]
              [?a :account/license ?ml]]
            (d/db conn) [:stripe-customer/customer-id id])
       (d/entity (d/db conn))))

(defn add-rent-payment!
  "Add a new rent payment entity to member's `license` based on `invoice-id`."
  [conn invoice-id customer-id period-start]
  (let [license (customer-id->member-license conn customer-id)
        payment (rent-payment/autopay-payment invoice-id
                                              period-start
                                              (member-license/rate license))]
    @(d/transact conn [(member-license/add-rent-payments license payment)])))
