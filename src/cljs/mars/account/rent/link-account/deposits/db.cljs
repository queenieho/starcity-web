(ns mars.account.rent.link-account.deposits.db
  (:require [clojure.string :as str]))

(def ^:private currencies-data
  ["USD" "AUD" "CAD" "EUR" "GBP" "DKK" "NOK" "SEK" "JPY" "SGD"])

(def path ::deposits)
(def default-value
  {path {:bank-info {:submitted  false
                     :submitting false
                     :currency   "USD"
                     :country    "US"
                     :errors     {}
                     :currencies currencies-data}
         :deposits  {:amounts [nil nil]}}})

;;; Bank Info Form

(defn- bank-info [db]
  (:bank-info db))

(defn validate-account-number
  [account-number country]
  (when-not (.validateAccountNumber js/Stripe.bankAccount account-number country)
    {:account-number "That is not a valid account number."}))

(defn validate-routing-number
  [routing-number country]
  (when-not (.validateRoutingNumber js/Stripe.bankAccount routing-number country)
    {:routing-number "That is not a valid routing number."}))

(defn update-form-field [db k v]
  (assoc-in db [:bank-info k] v))

(defn form-data [db]
  (select-keys (bank-info db) [:account-number
                               :routing-number
                               :currency
                               :country
                               :account-holder]))

(defn form-errors [db]
  (:errors db))

(defn set-form-errors [db errors]
  (assoc db :errors errors))

(defn account-holder [db]
  (:account-holder db))

(defn set-account-holder [db name]
  (assoc db :account-holder name))

;;; Deposits

(defn deposit-amounts [db]
  (get-in db [:deposits :amounts]))

(defn set-deposit-amount [db idx amount]
  (assoc-in db [:deposits :amounts idx] amount))

(defn can-submit-deposits? [db]
  (let [[amount-1 amount-2] (deposit-amounts db)]
    (and amount-1
         amount-2
         (pos? amount-1)
         (pos? amount-2))))

(defn submitting-deposits? [db]
  (get-in db [:deposits :submitting]))

(defn toggle-submitting-deposits [db]
  (update-in db [:deposits :submitting] not))

;;; Error Refreshing
;; TODO: Better name than `submitted`

(defn set-submitted
  "After the form has been submitted the errors need to be constantly refreshed.
  This flag indicates that this behavior should take place."
  [db]
  (assoc-in db [:bank-info :submitted] true))

(defn submitted? [db]
  (get-in db [:bank-info :submitted]))

(defn toggle-submitting-bank-info [db]
  (update-in db [:bank-info :submitting] not))

(defn submitting-bank-info? [db]
  (get-in db [:bank-info :submitting]))

(defn can-submit? [db]
  (let [{:keys [account-holder account-number
                routing-number country currency]} (form-data db)]
    (let [nb? (comp not str/blank?)]
      (and country
           currency
           (nb? account-holder)
           (nb? routing-number)
           (nb? account-number)))))

;;; Currencies

(defn currencies [db]
  (get-in db [:bank-info :currencies]))

;;; Countries

(defn countries [db]
  (:countries db))

(defn set-countries [db countries]
  (assoc db :countries countries))

;;; Stripe

(defn stripe-key [db]
  (get-in db [:stripe :public-key]))

(defn set-stripe-key [db key]
  (assoc-in db [:stripe :public-key] key))
