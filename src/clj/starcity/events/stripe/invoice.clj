(ns starcity.events.stripe.invoice
  (:require [clj-time
             [coerce :as c]
             [core :as t]]
            [clojure.core.async :refer [go <!]]
            [clojure.spec :as s]
            [dire.core :refer [with-pre-hook!]]
            [starcity.datomic :refer [conn]]
            [starcity.events.stripe.invoice
             [created :as created]
             [payment-failed :as payment-failed]
             [payment-succeeded :as payment-succeeded]]
            [starcity.events.util :as e :refer :all]
            [starcity.models
             [member-license :as member-license]
             [rent-payment :as rent-payment]]
            [taoensso.timbre :as timbre]))

;; =============================================================================
;; Internal
;; =============================================================================

(defn- entities
  [conn invoice-id]
  (let [payment (rent-payment/by-invoice-id conn invoice-id)
        license (rent-payment/member-license payment)
        account (member-license/account license)]
    {:payment payment :license license :account account}))

;; =============================================================================
;; Invoice Created

(defn created!
  "A new invoice has been created on Stripe, which means that a corresponding
  `rent-payment` entity should be created on our end to represent the autopay
  payment."
  [invoice-id customer-id period-start]
  (go (try
        (created/add-rent-payment! conn invoice-id customer-id period-start)
        (catch Throwable ex
          (timbre/error ex ::created {:invoice-id   invoice-id
                                      :customer-id  customer-id
                                      :period-start period-start})
          ex))))

(with-pre-hook! #'created!
  (fn [i c p] (timbre/info ::created {:invoice-id   i
                                     :customer-id  c
                                     :period-start p})))

(s/fdef created!
        :args (s/cat :invoice-id string? :customer-id string? :period-start inst?)
        :ret chan?)

;; =============================================================================
;; Invoice Payment Failed

(defn payment-failed!
  "An invoice payment has failed on Stripe, which means that the account's
  `rent-payment` entity should reflect the failure.

  We also want to notify ourselves and the customer of what's happened."
  [invoice-id]
  (go
    (try
      (let [{:keys [payment account license]} (entities conn invoice-id)
            failures (-> payment rent-payment/failures inc)
            res (payment-failed/failed! conn invoice-id payment failures)]
        (payment-failed/slack invoice-id account license failures)
        (payment-failed/notify-customer account failures)
        res)
      (catch Throwable ex
        (timbre/error ex ::failed {:invoice-id invoice-id})
        ex))))

(with-pre-hook! #'payment-failed!
  (fn [i] (timbre/info ::payment-failed {:invoice-id i})))

(s/fdef payment-failed!
        :args (s/cat :invoice-id string?)
        :ret chan?)

;; =============================================================================
;; Invoice Payment Succeeded

(defn payment-succeeded!
  "An invoice payment has succeeded on Stripe -- update the corresponding
  `rent-payment` entity and send appropriate notifications."
  [invoice-id]
  (go
    (try
      (let [{:keys [payment license account]} (entities conn invoice-id)
            res (payment-succeeded/paid! conn payment)]
        (payment-succeeded/slack invoice-id license account)
        (payment-succeeded/notify-customer account payment)
        res)
      (catch Throwable ex
        (timbre/error ex ::succeeded {:invoice-id invoice-id})
        ex))))

(with-pre-hook! #'payment-succeeded!
  (fn [i] (timbre/info ::payment-succeeded {:invoice-id i})))

(s/fdef payment-succeeded!
        :args (s/cat :invoice-id string?))




(comment

  (created! "in_19XImtJDow24Tc1az91yfzA4" "cus_9bzpu7sapb8g7y" (c/to-date (t/date-time 2017 1 1)))

  (payment-succeeded! "in_19XImtJDow24Tc1az91yfzA4")

  (payment-failed! "in_19XImtJDow24Tc1az91yfzA4")

  )
