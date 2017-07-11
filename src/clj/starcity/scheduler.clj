(ns starcity.scheduler
  (:require [datomic.api :as d]
            [hara.io.scheduler :as sch]
            [mount.core :refer [defstate]]
            [reactor.events :as events]
            [starcity.datomic :refer [conn]]
            [taoensso.timbre :as timbre]))


(def ^:private scheduler*
  (sch/scheduler
   {:create-rent-payments
    {:handler  #(d/transact conn [(events/create-monthly-rent-payments %)])
     ;; first of every month
     :schedule "0 0 0 * 1 * *"}}
   {}
   {:clock {:timezone "PST"}}))


(defstate scheduler
  :start (do
           (timbre/info ::starting)
           (sch/start! scheduler*))
  :stop (do
          (timbre/info ::stopping)
          (sch/stop! scheduler)))
