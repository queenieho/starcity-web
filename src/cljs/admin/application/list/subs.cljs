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
 :application.list/header-keys
 :<- [root-db-key]
 ;; TODO: Move to init
 (fn [{ks :header-keys} _]
   ks))

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
