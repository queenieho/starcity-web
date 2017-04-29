(ns starcity.models.msg
  (:require [clojure.spec :as s]
            [datomic.api :as d]
            [starcity.datomic :refer [tempid]]
            [taoensso.nippy :as nippy]
            [toolbelt.predicates :as p]))

;; =============================================================================
;; Transactions
;; =============================================================================

(s/def :msg/key keyword?)
(s/def ::msg
  (s/keys :req [:db/id :msg/uuid :msg/key]
          :opt [:msg/params]))

(defn create
  "Create a new msg."
  [msg-key & {:keys [params]}]
  (let [data {:db/id    (tempid)
              :msg/uuid (d/squuid)
              :msg/key  msg-key}]
    (if-not params
      data
      (assoc data :msg/params (nippy/freeze params)))))

(s/fdef create
        :args (s/cat :key keyword?
                     :opts (s/keys* :opt-un [::params]))
        :ret (s/keys :req [:db/id
                           :msg/uuid
                           :msg/key]
                     :opt [:msg/params]))


;; =============================================================================
;; Named
;; =============================================================================

;; =============================================================================
;; Accounts

(def approved-key :account/approve)

(defn approved
  "An account has been approved."
  [approver approvee unit license move-in]
  (create approved-key :params {:approver-id (:db/id approver)
                                :approvee-id (:db/id approvee)
                                :unit-id     (:db/id unit)
                                :license-id  (:db/id license)
                                :move-in     move-in}))

(s/fdef approved
        :args (s/cat :approver p/entity?
                     :approvee p/entity?
                     :unit p/entity?
                     :license p/entity?
                     :move-in inst?)
        :ret ::msg)

(def account-created-key :account/created)

(defn account-created
  "An account has been created."
  [email]
  (create account-created-key :params {:email email}))

(s/fdef account-created
        :args (s/cat :email string?)
        :ret ::msg)

(def promoted-key :account/promoted)

(defn promoted
  "An `account` has been promoted to membership status."
  [account]
  (create promoted-key :params {:account-id (:db/id account)}))

(s/fdef promoted
        :args (s/cat :account p/entity?)
        :ret ::msg)

;; =============================================================================
;; Notes

(def note-created-key :note/created)

(defn note-created
  "A new note has been created."
  [note & [notify?]]
  (create note-created-key :params {:note/uuid (:note/uuid note)
                                    :notify?   notify?}))

(s/fdef note-created
        :args (s/cat :note (s/keys :req [:note/uuid]) :notify? (s/? boolean?))
        :ret ::msg)

(def note-comment-created-key :note.comment/created)

(defn note-comment-created
  [comment & [notify?]]
  (create note-comment-created-key :params {:comment/uuid (:note/uuid comment)
                                            :notify?      notify?}))

(s/fdef note-comment-created
        :args (s/cat :comment (s/keys :req [:note/uuid]) :notify? (s/? boolean?))
        :ret ::msg)

;; =============================================================================
;; Rent

(def ach-payment-key :rent.payment.ach/pay)

(defn ach-payment
  "An ACH payment has been made."
  [payment account]
  (create ach-payment-key :params {:payment-id (:db/id payment)
                                   :account-id (:db/id account)}))

(s/fdef ach-payment
        :args (s/cat :payment p/entity? :account p/entity?)
        :ret ::msg)

(def rent-payments-created-key :rent.payments/created)

(defn rent-payments-created
  "Rent payments have been created for `licenses`."
  [license-ids]
  (create rent-payments-created-key :params license-ids))

(s/fdef rent-payments-created
        :args (s/cat :licenses (s/spec (s/+ integer?)))
        :ret ::msg)

;; =============================================================================
;; Charges

(def charge-succeeded-key :stripe.charge/succeeded)

(defn charge-succeeded
  "A Stripe charge has succeeded."
  [charge-id]
  (create charge-succeeded-key :params charge-id))

(s/fdef charge-succeeded
        :args (s/cat :charge-id string?)
        :ret ::msg)

(def charge-failed-key :stripe.charge/failed)

(defn charge-failed
  "A Stripe charge has failed."
  [charge-id]
  (create charge-failed-key :params charge-id))

(s/fdef charge-failed
        :args (s/cat :charge-id string?)
        :ret ::msg)

(def customer-source-updated-key :stripe.customer.source/updated)

(defn customer-source-updated
  "`account`'s bank verification failed."
  [customer-id status]
  (create customer-source-updated-key :params {:customer-id customer-id
                                               :status      status}))

;; =============================================================================
;; Autopay

(def invoice-created-key :stripe.invoice/created)

(defn invoice-created
  [invoice-id customer-id period-start]
  (create invoice-created-key :params {:invoice-id   invoice-id
                                       :customer-id  customer-id
                                       :period-start period-start}))

(s/fdef invoice-created
        :args (s/cat :invoice-id string?
                     :customer-id string?
                     :period-start inst?)
        :ret ::msg)

(def invoice-payment-failed-key :stripe.invoice.payment/failed)

(defn invoice-payment-failed
  [invoice-id]
  (create invoice-payment-failed-key :params invoice-id))

(s/fdef invoice-payment-failed
        :args (s/cat :invoice-id string?)
        :ret ::msg)

(def invoice-payment-succeeded-key :stripe.invoice.payment/succeeded)

(defn invoice-payment-succeeded
  [invoice-id]
  (create invoice-payment-succeeded-key :params invoice-id))

(s/fdef invoice-payment-succeeded
        :args (s/cat :invoice-id string?)
        :ret ::msg)

(def autopay-will-begin-key :stripe.subscription/autopay-will-begin)

(defn autopay-will-begin
  [member-license]
  (create autopay-will-begin-key :params (:db/id member-license)))

(s/fdef autopay-will-begin
        :args (s/cat :member-license p/entity?)
        :ret ::msg)

(def autopay-deactivated-key :stripe.subscription/autopay-deactivated)

(defn autopay-deactivated
  [member-license]
  (create autopay-deactivated-key :params (:db/id member-license)))

(s/fdef autopay-deactivated
        :args (s/cat :member-license p/entity?)
        :ret ::msg)

;; =============================================================================
;; Security Deposit


(def deposit-payment-made-key :security-deposit/ach-payment-made)

(defn deposit-payment-made
  "A security deposit payment has been made."
  [account charge]
  (create deposit-payment-made-key :params {:charge-id  (:db/id charge)
                                            :account-id (:db/id account)}))

(s/fdef deposit-payment-made
        :args (s/cat :account p/entity? :charge p/entity?)
        :ret ::msg)

(def remainder-deposit-paid-key :security-deposit.remainder/paid)

(defn remainder-deposit-paid
  "The remainder of the security deposit has been paid."
  [account charge-id]
  (create remainder-deposit-paid-key :params {:charge-id  charge-id
                                              :account-id (:db/id account)}))

(s/fdef remainder-deposit-paid
        :args (s/cat :account p/entity? :charge-id string?)
        :ret ::msg)
