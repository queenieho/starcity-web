(ns mars.account.rent.db
  (:require [mars.account.rent.link-account.db :as link-account]
            [mars.account.rent.history.db :as history]
            [cljs.spec :as s]
            [cljs-time.core :as t]
            [cljs-time.coerce :as c]
            [clojure.string :as str]
            [starcity.utils :refer [find-by]]))

(def path ::rent)
(def default-value
  (merge {path {:autopay      {:showing         false
                               :enabled         false
                               :fetching-status false
                               :enabling        false}

                :make-payment {:showing false
                               :paying  false}

                :link-account {:showing false}

                :bank-account {:loading true
                               :account {:bank-name ""
                                         :number    ""}}

                :upcoming     {:loading true
                               :payment {:amount 0
                                         :due-by (t/now)}}}}
         link-account/default-value
         history/default-value))

;; =============================================================================
;; Link Account

(defn showing-link-account? [db]
  (get-in db [:link-account :showing]))

(defn toggle-link-account [db]
  (update-in db [:link-account :showing] not))

;; =============================================================================
;; Upcoming Payment

(defn set-upcoming-loading [db to]
  (assoc-in db [:upcoming :loading] to))

(defn upcoming-loading? [db]
  (get-in db [:upcoming :loading]))

(defn upcoming-payment [db]
  (get-in db [:upcoming :payment]))

(defn set-upcoming-payment [db amount due-by]
  (assoc-in db [:upcoming :payment]
            {:amount amount
             :due-by (c/to-date-time due-by)}))

;; =============================================================================
;; Bank Account

(defn set-bank-account-loading [db to]
  (assoc-in db [:bank-account :loading] to))

(defn bank-account-loading? [db]
  (get-in db [:bank-account :loading]))

(defn set-bank-account [db bank-account]
  (assoc-in db [:bank-account :account] bank-account))

(defn bank-account [db]
  (get-in db [:bank-account :account]))

(defn bank-account-linked? [db]
  (let [{:keys [bank-name number]} (bank-account db)]
    (and (not (str/blank? bank-name))
         (not (str/blank? number)))))

;; =============================================================================
;; Enable Autopay

(defn toggle-show-autopay [db]
  (update-in db [:autopay :showing] not))

(defn showing-enable-autopay? [db]
  (get-in db [:autopay :showing]))

(defn autopay-enabled? [db]
  (get-in db [:autopay :enabled]))

(defn set-autopay-enabled [db to]
  (assoc-in db [:autopay :enabled] to))

(defn toggle-fetching-autopay-status [db]
  (update-in db [:autopay :fetching-status] not))

(defn fetching-autopay-status? [db]
  (get-in db [:autopay :fetching-status]))

(defn toggle-enabling-autopay [db]
  (update-in db [:autopay :enabling] not))

(defn enabling-autopay? [db]
  (get-in db [:autopay :enabling]))

;; =============================================================================
;; Make Payment

(defn showing-make-payment? [db]
  (get-in db [:make-payment :showing]))

(defn toggle-show-make-payment [db]
  (update-in db [:make-payment :showing] not))

(defn set-payment [db payment]
  (assoc-in db [:make-payment :payment] payment))

(defn rent-payment [db]
  (get-in db [:make-payment :payment]))

(defn paying? [db]
  (get-in db [:make-payment :paying]))

(defn toggle-paying [db]
  (update-in db [:make-payment :paying] not))
