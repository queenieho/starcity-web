(ns starcity.events.stripe.charge.common
  (:require [starcity.models.charge :as charge]))

(defn charge-type
  [conn charge]
  (cond
    (charge/is-security-deposit-charge? conn charge) :security-deposit
    (charge/is-rent-ach-charge? conn charge)         :rent
    :otherwise                                       :default))
