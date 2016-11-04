(ns admin.account.list.subs
  (:require [admin.account.list.db :refer [root-db-key]]
            [re-frame.core :refer [reg-sub]]
            [starcity.dates :as d]))

(reg-sub
 root-db-key
 (fn [db _]
   (get db root-db-key)))

(reg-sub
 :account.list/header
 :<- [root-db-key]
 (fn [{header :header} _]
   header))

(reg-sub
 :account.list/sort
 :<- [root-db-key]
 (fn [data _]
   (:sort data)))

(reg-sub
 :account.list/list
 :<- [root-db-key]
 (fn [data _]
   (:list data)))

(reg-sub
 :account.list.view/current
 :<- [root-db-key]
 (fn [data _]
   (:view data)))

(reg-sub
 :account.list.view/available
 :<- [root-db-key]
 (fn [data _]
   (:views data)))

(reg-sub
 :account.list/loading?
 :<- [root-db-key]
 (fn [data _]
   (:loading data)))

(reg-sub
 :account.list.pagination/num-pages
 :<- [root-db-key]
 (fn [{:keys [pagination total]} _]
   (int (Math/ceil (/ total (:limit pagination))))))

(reg-sub
 :account.list.pagination/page-num
 :<- [root-db-key]
 (fn [{{offset :offset limit :limit} :pagination} _]
   (int (Math/floor (/ offset limit)))))

(reg-sub
 :account.list.pagination/has-previous?
 :<- [root-db-key]
 (fn [{{offset :offset limit :limit} :pagination} _]
   (>= offset limit)))

(reg-sub
 :account.list.pagination/has-next?
 :<- [:account.list.pagination/num-pages]
 :<- [:account.list.pagination/page-num]
 (fn [[num-pages page-num] _]
   (< (inc page-num) num-pages)))
