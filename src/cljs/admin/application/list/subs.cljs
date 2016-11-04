(ns admin.application.list.subs
  (:require [admin.application.list.db :refer [root-db-key]]
            [re-frame.core :refer [reg-sub]]
            [starcity.dates :as d]))

(reg-sub
 root-db-key
 (fn [db _]
   (get db root-db-key)))

;; Ordered keys identifying values in application map
(reg-sub
 :application.list/header
 :<- [root-db-key]
 (fn [{header :header} _]
   header))

(reg-sub
 :application.list/list
 :<- [root-db-key]
 (fn [data _]
   (:list data)))

(reg-sub
 :application.list/sort
 :<- [root-db-key]
 (fn [data _]
   (:sort data)))
