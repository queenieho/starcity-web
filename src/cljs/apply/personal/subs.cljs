(ns apply.personal.subs
  (:require [re-frame.core :refer [reg-sub]]
            [apply.personal.models :as m]))

(reg-sub
 :personal/background
 (fn [db _]
   (get db :personal/background {})))

(reg-sub
 :personal.background/complete?
 :<- [:personal/background]
 (fn [info _]
   (m/background-complete? info)))

(reg-sub
 :personal/phone-number
 (fn [db _]
   (get db :personal/phone-number "")))

(reg-sub
 :personal.phone-number/complete?
 :<- [:personal/phone-number]
 (fn [phone-number _]
   (m/phone-number-complete? phone-number)))

(reg-sub
 :personal/income
 (fn [db _]
   (get db :personal/income)))

(reg-sub
 :personal.income/complete?
 :<- [:personal/income]
 (fn [files _]
   (not (nil? files))))
