(ns starcity.services.stripe.connect
  (:require [clj-time
             [coerce :as c]
             [core :as t]]
            [clojure.spec :as s]
            [starcity spec
             [config :refer [config]]]
            [starcity.services.stripe.request :refer [request]]
            [datomic.api :as d]))

;; =============================================================================
;; API
;; =============================================================================

;; =============================================================================
;; Create a Managed Account

;; =====================================
;; Owner

(defn- dob* [day month year]
  {"legal_entity[dob][day]"   day
   "legal_entity[dob][month]" month
   "legal_entity[dob][year]"  year})

(defn owner
  [first-name last-name dob]
  (merge {"legal_entity[first_name]" first-name
          "legal_entity[last_name]"  last-name}
         (dob* (t/day dob) (t/month dob) (t/year dob))))

(s/fdef owner
        :args (s/cat :first-name string?
                     :last-name string?
                     :dob :starcity.spec/datetime))

;; =====================================
;; Business

(defn address
  [line1 zip & {:keys [line2 city state]
                :or   {city "San Francisco", state "CA"}}]
  {"legal_entity[address][city]"        city
   "legal_entity[address][state]"       state
   "legal_entity[address][postal_code]" zip
   "legal_entity[address][line1]"       line1
   "legal_entity[address][line2]"       line2
   "legal_entity[address][country]"     "US"})

(def default-address
  "Default address that we create building entities under."
  (address "995 Market St." "94103" :line2 "2nd Fl."))

(defn business
  [name tax-id & [address]]
  (merge {"legal_entity[business_name]"   name
          "legal_entity[business_tax_id]" tax-id
          "legal_entity[type]"            "company"}
         (or address default-address)))

;; =====================================
;; Account

(defn account
  [account-number routing-number]
  {"external_account[account_number]" account-number
   "external_account[routing_number]" routing-number
   "external_account[object]"         "bank_account"
   "external_account[country]"        "US"
   "external_account[currency]"       "USD"})

(defn create-account! [owner business account]
  (request {:endpoint "accounts"
            :method   :post}
           (merge
            {:country               "US"
             :managed               true
             "tos_acceptance[ip]"   (get-in config [:stripe :tos-ip])
             "tos_acceptance[date]" (int (/ (c/to-long (t/now)) 1000))}
            owner
            business
            account)))

;; =============================================================================
;; Create a Customer on the managed account

(defn create-token
  "Create a bank account token that can be attached to a managed account
  customer."
  [customer-id bank-token managed-account-id]
  (request {:endpoint        "tokens"
            :method          :post
            :managed-account managed-account-id}
           {:customer     customer-id
            :bank_account bank-token}))

;; =============================================================================
;; Fetch Accounts

(defn fetch-accounts []
  (request {:endpoint "accounts" :method :get} {}))
