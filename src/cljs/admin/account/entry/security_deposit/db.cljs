(ns admin.account.entry.security-deposit.db
  (:require [starcity.utils.model :refer [get-in-db*
                                          assoc-in-db*]]))

(def root-db-key :account.entry/security-deposit)

(def ^:private assoc-in-db (assoc-in-db* root-db-key))
(def ^:private get-in-db (get-in-db* root-db-key))

(def default-value
  {:loading             false
   :data                {}
   :check-statuses      ["deposited" "cleared" "bounced" "cancelled" "received"]
   :showing-check-modal false
   :check-modal-data    {}})

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

(defn show-check-modal
  [db]
  (assoc-in-db db [:showing-check-modal] true))

(defn hide-check-modal
  [db]
  (assoc-in-db db [:showing-check-modal] false))

(defn check-modal-data
  [db]
  (get-in-db db [:check-modal-data]))

(defn set-check-modal-data
  ([db]
   (set-check-modal-data db {}))
  ([db data]
   (assoc-in-db db [:check-modal-data] data)))

(defn update-check-data
  [db k v]
  (assoc-in-db db [:check-modal-data k] v))
