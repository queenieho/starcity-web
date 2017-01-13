(ns starcity.datomic.seed.rent-payments
  (:require [clj-time
             [coerce :as c]
             [core :as t]]
            [datomic.api :as d]
            [starcity.models
             [check :as check]
             [member-license :as member-license]
             [rent-payment :as rp]]))

(defn- date [y m d]
  (c/to-date (t/date-time y m d)))

(def check-december
  (let [start (date 2016 12 1)
        end   (date 2016 12 31)]
    (rp/create 2000.0 start end :rent-payment.status/paid
               :method rp/check
               :check (check/create "Member" 2000.0 (java.util.Date.) 1175)
               :paid-on (date 2016 12 3))))

(def check-november-partial
  (let [start (date 2016 11 15)
        end   (date 2016 11 30)]
    (rp/create 1000.0 start end :rent-payment.status/paid
               :method rp/check
               :check (check/create "Member" 2000.0 (java.util.Date.) 1174)
               :due-date (date 2016 11 20)
               :paid-on (date 2016 11 19))))

(def check-november-other
  (rp/create 1000.0 (date 2016 11 15) (date 2016 11 30) :rent-payment.status/paid
             :method rp/other
             :due-date (date 2016 11 20)
             :paid-on (date 2016 11 19)
             :desc "bill.com"))

(defn- tx-data [conn]
  (let [license (->> (d/entity (d/db conn) [:account/email "member@test.com"])
                     (member-license/active conn))]
    [(member-license/add-rent-payments
      license
      (rp/create 2000.0 (date 2017 1 1) (date 2017 1 31) :rent-payment.status/due)
      check-december
      check-november-other)]))

(defn seed [conn]
  #_@(d/transact conn (tx-data conn)))
