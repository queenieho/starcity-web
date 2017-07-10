(ns starcity.models.msg
  (:require [clojure.spec :as s]
            [datomic.api :as d]
            [starcity.datomic.partition :refer [tempid]]
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
;; Notes

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
