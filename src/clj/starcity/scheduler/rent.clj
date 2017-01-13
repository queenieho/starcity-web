(ns starcity.scheduler.rent
  (:require [starcity.events.rent :as rent]))

(defn create-rent-payments
  "Create rent payments for every active member that is not on autopay."
  [t]
  (rent/create-monthly-rent-payments! t))
