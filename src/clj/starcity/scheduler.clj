(ns starcity.scheduler
  (:require [hara.io.scheduler :as sch]
            [mount.core :refer [defstate]]
            [starcity.scheduler.rent :as rent]
            [clj-time.coerce :as c]
            [clj-time.core :as t]
            [datomic.api :as d]
            [starcity.models.account :as account]
            [starcity.models.member-license :as member-license]
            [taoensso.timbre :as timbre]))

(def ^:private scheduler*
  (sch/scheduler
   {:create-rent-payments
    {:handler rent/create-rent-payments
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
