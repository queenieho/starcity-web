(ns admin.account.entry.security-deposit.db
  (:require [starcity.utils.model :refer [get-in-db*
                                          assoc-in-db*]]))

(def root-db-key :account.entry/security-deposit)

(def ^:private assoc-in-db (assoc-in-db* root-db-key))
(def ^:private get-in-db (get-in-db* root-db-key))

(def default-value
  {:loading false
   :data    {}})

(defn security-deposit
  [db]
  (get-in-db db [:data]))

(defn set-security-deposit
  [db data]
  (assoc-in-db db [:data] data))

(defn loading
  [db]
  (get-in-db db [:loading]))

(defn set-loading
  ([db]
   (update-in db [root-db-key :loading] not))
  ([db v]
   (assoc-in-db db [:loading] v)))
