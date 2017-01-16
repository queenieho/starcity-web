(ns starcity.events.rent
  (:require [clojure.core.async :as a :refer [go]]
            [clojure.spec :as s]
            [dire.core :refer [with-pre-hook!]]
            [starcity
             [datomic :refer [conn]]
             [util :refer [entity?]]]
            [starcity.events.rent.make-ach-payment :as ach]
            [starcity.events.util :refer :all]
            [taoensso.timbre :as timbre]
            [datomic.api :as d]
            [clj-time.coerce :as c]
            [clj-time.core :as t]
            [starcity.models.rent-payment :as rent-payment]
            [starcity.models.member-license :as member-license]
            [starcity.services.mailgun :as mail]
            [starcity.models.account :as account]
            [starcity.services.mailgun.senders :as ms]
            [starcity.services.mailgun.message :as mm]
            [starcity.config :as config]))

;; =============================================================================
;; Make ACH Payment

(defn- create-ach-charge!
  "Create the charge on Stripe and corresponding rent payment."
  [account payment]
  (go-try
   (let [charge-id (ach/create-charge! conn account payment)]
     (ach/create-payment conn charge-id account payment)
     charge-id)))

(defn make-ach-payment!
  "Trigger event signifying that member with `account` would like to pay
  `payment` using ACH."
  [account payment]
  (go
    (try
      (let [charge-id (<!? (create-ach-charge! account payment))]
        (ach/slack charge-id account payment))
      (catch Throwable ex
        (timbre/error ex ::make-payment {:account   (:db/id account)
                                         :payment   (:db/id payment)})
        ex))))

(with-pre-hook! #'make-ach-payment!
  (fn [a p] (timbre/info ::make-payment {:account (:db/id a)
                                        :payment (:db/id p)})))

(s/fdef make-ach-payment!
        :args (s/cat :account entity? :payment entity?)
        :ret chan?)

;; =============================================================================
;; Create Monthly Rent Payments

(defn- query-active-licenses [conn]
  (d/q '[:find ?e ?p
         :where
         ;; active licenses
         [?e :member-license/active true]
         [?e :member-license/price ?p]
         ;; not on autopay
         [(missing? $ ?e :member-license/subscription-id)]]
       (d/db conn)))

(defn- period-end [t]
  (-> t c/to-date-time t/last-day-of-the-month c/to-date))

(defn- license-txes [start licenses]
  (mapv
   (fn [[e amount]]
     (let [p (rent-payment/create amount start (period-end start) :rent-payment.status/due)]
       {:db/id                        e
        :member-license/rent-payments p}))
   licenses))

(defn send-reminder [account]
  (mail/send (account/email account) "Reminder: Your Rent is Due"
             (mm/msg
              (mm/greeting (account/first-name account))
              (mm/p "It's that time again! Your rent payment is <b>due by the 5th</b>.")
              (mm/p "Please log into your member dashboard "
                    [:a {:href (str config/hostname "/me/account/rent")} "here"]
                    " to pay your rent with ACH. <b>If you'd like to stop getting these reminders, sign up for autopay while you're there!</b>")
              (mm/signature))
             :from ms/noreply))

(defn- send-reminders [conn license-ids]
  (let [accounts (map (comp member-license/account (partial d/entity (d/db conn))) license-ids)]
    (doseq [account accounts]
      (send-reminder account))))

(defn create-monthly-rent-payments!
  "Once per month all of the rent payments need to be created for active members
  that are not on Autopay -- this event creates those payments and sends email
  reminders to the members."
  [period]
  (go
    (try
      (let [txes (->> (query-active-licenses conn) (license-txes period))]
        @(d/transact conn txes)
        (send-reminders conn (map :db/id txes))
        :ok)
      (catch Throwable ex
        (timbre/error ex ::create-monthly-rent-payments {:period period})
        ex))))

(with-pre-hook! #'create-monthly-rent-payments!
  (fn [period] (timbre/info ::create-monthly-rent-payments {:period period})))

(comment
  (create-monthly-rent-payments!
   (c/to-date (t/date-time 2017 2 1)))

  )
