(ns starcity.scheduler
  (:require [datomic.api :as d]
            [hara.io.scheduler :as sch]
            [mount.core :refer [defstate]]
            [starcity.datomic :refer [conn]]
            [starcity.models.cmd :as cmd]
            [taoensso.timbre :as timbre]))

(def ^:private scheduler*
  (sch/scheduler
   {:create-rent-payments
    {:handler #(d/transact conn [(cmd/create-rent-payments %)])
     :schedule "0 0 0 * 1 * *"          ; first of every month
     }}
   {}
   {:clock {:timezone "PST"}}))

(defstate scheduler
  :start (do
           (timbre/info ::starting)
           (sch/start! scheduler*))
  :stop (do
          (timbre/info ::stopping)
          (sch/stop! scheduler)))

(comment
  (d/transact conn [(cmd/create-rent-payments #inst "2017-03-01T00:00:00.000-00:00")])

  )
