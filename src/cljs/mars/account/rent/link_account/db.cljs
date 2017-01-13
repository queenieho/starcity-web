(ns mars.account.rent.link-account.db
  (:require [mars.account.rent.link-account.authorize.db :as authorize]
            [mars.account.rent.link-account.deposits.db :as deposits]))

(def path ::setup)
(def default-value
  (merge {path {:status      :init
                :loading     false
                :subscribing false}}
         authorize/default-value
         deposits/default-value))

(defn status [db]
  (:status db))

(defn set-status [db status]
  (if (string? status)
    (assoc db :status (keyword status))
    (assoc db :status status)))

;;; UI

(defn loading [db]
  (:loading db))

(defn toggle-loading [db]
  (update db :loading not))

;;; Plaid

(defn plaid-key [db]
  (get-in db [:plaid :public-key]))

(defn set-plaid-key [db key]
  (assoc-in db [:plaid :public-key] key))

(defn plaid-env [db]
  (get-in db [:plaid :env]))

(defn set-plaid-env [db env]
  (assoc-in db [:plaid :env] env))

(defn toggle-plaid-loading [db]
  (update-in db [:plaid :loading] not))

(defn plaid-loading [db]
  (get-in db [:plaid :loading]))
