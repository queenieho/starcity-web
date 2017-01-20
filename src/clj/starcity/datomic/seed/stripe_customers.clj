(ns starcity.datomic.seed.stripe-customers
  (:require [starcity.datomic.partition :refer [tempid]]
            [datomic.api :as d]))

(def tx-data
  [{:db/id                              (tempid)
    :stripe-customer/account            [:account/email "member@test.com"]
    :stripe-customer/customer-id        "cus_9bzpu7sapb8g7y"
    :stripe-customer/bank-account-token "ba_19IlpVIvRccmW9nO20kCxqE5"}
   ;; {:db/id                              (tempid)
   ;;  :stripe-customer/account            [:account/email "member@test.com"]
   ;;  :stripe-customer/customer-id        "cus_9kIQ5KHv9rncsN"
   ;;  :stripe-customer/bank-account-token "ba_19QnpDJDow24Tc1ahbRpFSRE"
   ;;  :stripe-customer/managed            [:property/internal-name "52gilbert"]}
   ])

(defn seed [conn]
  #_@(d/transact conn tx-data))
