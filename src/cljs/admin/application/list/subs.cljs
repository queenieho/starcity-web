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
 :application.list/sort
 :<- [root-db-key]
 (fn [data _]
   (:sort data)))

(reg-sub
 :application.list/list
 :<- [root-db-key]
 (fn [data _]
   (:list data)))

(reg-sub
 :application.list.view/current
 :<- [root-db-key]
 (fn [data _]
   (:view data)))

(reg-sub
 :application.list.view/available
 :<- [root-db-key]
 (fn [data _]
   (:views data)))

(reg-sub
 :application.list/loading?
 :<- [root-db-key]
 (fn [data _]
   (:loading data)))

(reg-sub
 :application.list/query
 :<- [root-db-key]
 (fn [data _]
   (:query data)))

(reg-sub
 :application.list.pagination/num-pages
 :<- [root-db-key]
 (fn [{:keys [pagination total]} _]
   (int (Math/ceil (/ total (:limit pagination))))))

(reg-sub
 :application.list.pagination/page-num
 :<- [root-db-key]
 (fn [{{offset :offset limit :limit} :pagination} _]
   (int (Math/floor (/ offset limit)))))

(reg-sub
 :application.list.pagination/has-previous?
 :<- [root-db-key]
 (fn [{{offset :offset limit :limit} :pagination} _]
   (>= offset limit)))

(reg-sub
 :application.list.pagination/has-next?
 :<- [:application.list.pagination/num-pages]
 :<- [:application.list.pagination/page-num]
 (fn [[num-pages page-num] _]
   (< (inc page-num) num-pages)))
