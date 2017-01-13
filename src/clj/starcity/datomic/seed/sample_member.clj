(ns starcity.datomic.seed.sample-member
  (:require [clj-time
             [coerce :as c]
             [core :as t]]
            [datomic.api :as d]
            [starcity.datomic.partition :refer [tempid]]
            [starcity.events.promote :refer [member-tx]]
            [starcity.models.rent-payment :as rent-payment]
            [starcity.models.member-license :as member-license]))

(def property [:property/internal-name "52gilbert"])

(defn- create-security-deposit [account]
  {:db/id                            (tempid)
   :security-deposit/account         account
   :security-deposit/amount-required 2000
   :security-deposit/amount-received 2000
   :security-deposit/payment-method  :security-deposit.payment-method/ach
   :security-deposit/payment-type    :security-deposit.payment-type/full})

(defn seed [conn]
  (let [member  (d/entity (d/db conn) [:account/email "member@test.com"])
        unit    (-> (d/entity (d/db conn) property) :property/units first)
        license (->> (d/q '[:find ?e . :where [?e :license/term 3]]
                          (d/db conn))
                     (d/entity (d/db conn)))]
    @(d/transact conn (concat
                       [(create-security-deposit (:db/id member))]
                       (map (comp create-security-deposit (partial conj [:account/email]))
                            ["jon@test.com" "jesse@test.com" "mo@test.com" "meg@test.com"])
                       (member-tx conn member license unit (java.util.Date.) 2000.0)))))

(comment
  (let [conn    starcity.datomic/conn
        member  (d/entity (d/db conn) [:account/email "member@test.com"])
        unit    (-> (d/entity (d/db conn) property) :property/units first)
        license (->> (d/q '[:find ?e . :where [?e :license/term 3]]
                          (d/db conn))
                     (d/entity (d/db conn)))]
    ;; (member-license/active conn member)
    ;; (rent-payment/payment-within conn member (java.util.Date.))
    (member-tx conn member license unit (java.util.Date.) 2000.0))

  (d/touch *1)

  )
