(ns admin.account.entry.model
  (:require [admin.account.entry.db :refer [root-db-key]]
            [starcity.utils.model :refer [get-in-db*
                                          assoc-in-db*]]
            [starcity.log :as l]))

(def ^:private assoc-in-db (assoc-in-db* root-db-key))
(def ^:private get-in-db (get-in-db* root-db-key))

(defn current-account-id
  "Retrieve or set the the current account-id in the db."
  ([db]
   (get-in-db db [:current-id]))
  ([db id]
   (assoc-in-db db [:current-id] id)))

(defn current-account
  "Retrieve or set the current account in the db."
  ([db]
   (let [id (current-account-id db)]
     (get-in-db db [:accounts id])))
  ([db account]
   (let [id (get account :id)]
     (-> (assoc-in-db db [:accounts id] account)
         (current-account-id id)))))

(defn full-name
  "Retrieve the full name associated with the current account."
  [db]
  (let [account (current-account db)]
    (:full-name account)))

(defn toggle-account-loading
  "Indicate that the account is being fetched from the server."
  [db]
  (update-in db [root-db-key :loading :account] not))

(defn is-account-loading?
  "Is the account still being fetched from the server?"
  [db]
  (get-in-db db [:loading :account]))

;; Simple selectors for extracting portions of the account

(def role (comp :role current-account))
(def phone-number (comp :phone-number current-account))
(def email (comp :email current-account))
