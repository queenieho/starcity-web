(ns starcity.observers.slack
  (:require [datomic.api :as d]
            [starcity.models
             [account :as account]
             [charge :as charge]
             [msg :as msg]
             [rent-payment :as rent-payment]
             [security-deposit :as deposit]]
            [starcity.services.slack :as slack]
            [starcity.services.slack.message :as sm]
            [taoensso.timbre :as timbre]
            [toolbelt.date :as td]
            [starcity.models.member-license :as member-license]
            [starcity.config :as config]
            [starcity.models.unit :as unit]
            [starcity.models.property :as property]
            [starcity.models.note :as note]
            [clojure.string :as string]
            [clojure.set :as set]))

(defmulti handle (fn [_ msg] (:msg/key msg)))

(defmethod handle :default
  [_ msg]
  (timbre/debug ::no-handler msg))

;; =============================================================================
;; Approve
;; =============================================================================

(defn- unit-link [unit property]
  (let [url (format "%s/admin/properties/%s/units/%s"
                    config/hostname
                    (:db/id property)
                    (:db/id unit))]
    (sm/link url (:unit/name unit))))

(defn- property-link [property]
  (let [url (format "%s/admin/properties/%s" config/hostname (:db/id property))]
    (sm/link url (property/name property))))

(defmethod handle msg/approved-key
  [conn {params :msg/params :as msg}]
  (let [{:keys [approver-id approvee-id unit-id license-id move-in]} params

        db       (d/db conn)
        approver (d/entity db approver-id)
        approvee (d/entity db approvee-id)
        unit     (d/entity db unit-id)
        license  (d/entity db license-id)
        property (unit/property unit)]
    (slack/community
     (sm/msg
      (sm/info
       (sm/text
        (format "*%s* has approved *%s* for membership! _Don't forget to send an email to let %s know that they've been approved._"
                (account/full-name approver)
                (account/full-name approvee)
                (account/first-name approvee)))
       (sm/fields
        (sm/field "Email" (account/email approvee) true)
        (sm/field "Property" (property-link property) true)
        (sm/field "Unit" (unit-link unit property) true)
        (sm/field "Move-in" (td/short-date move-in) true)
        (sm/field "Term" (format "%s months" (:license/term license)) true))))
     :uuid (:msg/uuid msg))))

;; =============================================================================
;; ACH Payment Made
;; =============================================================================

(defmethod handle msg/ach-payment-key
  [conn {params :msg/params :as msg}]
  (let [payment   (d/entity (d/db conn) (:payment-id params))
        account   (d/entity (d/db conn) (:account-id params))
        charge-id (get-in payment [:rent-payment/charge :charge/stripe-id])]
    (slack/ops
     (sm/msg
      (sm/success
       (sm/title "View Payment on Stripe"
                 (format "https://dashboard.stripe.com/payments/%s" charge-id))
       (sm/text (format "%s has paid his/her rent via ACH" (account/full-name account)))
       (sm/fields
        (sm/field "Amount"
                  (str "$" (rent-payment/amount payment))
                  true)
        (sm/field "Period Start"
                  (td/short-date (rent-payment/period-start payment))
                  true)
        (sm/field "Period End"
                  (td/short-date (rent-payment/period-end payment))
                  true))))
     :uuid (:msg/uuid msg))))

;; =============================================================================
;; Remainder security deposit paid
;; =============================================================================

(defmethod handle msg/remainder-deposit-paid-key
  [conn {params :msg/params :as msg}]
  (let [account (d/entity (d/db conn) (:account-id params))
        charge  (d/entity (d/db conn) [:charge/stripe-id (:charge-id params)])]
    (slack/ops
     (sm/msg
      (sm/success
       (sm/title "View Payment on Stripe"
                 (format "https://dashboard.stripe.com/payments/%s" (:charge-id params)))
       (sm/text (format "%s has paid the remainder of his/her security deposit"
                        (account/full-name account)))
       (sm/fields
        (sm/field "Method" "ACH" true)
        (sm/field "Amount" (str "$" (:charge/amount charge)) true))))
     :uuid (:msg/uuid msg))))

;; =============================================================================
;; Stripe
;; =============================================================================

;; =============================================================================
;; Charge

;; TODO: This is non-trivial in the case of rent payments -- we need to know
;; whether or not the charge came from the connected account or the master account.

;; (defn- charge-dashboard-url [managed-id invoice-id]
;;   (format "https://dashboard.stripe.com/%s/invoices/%s" managed-id invoice-id))

;; =====================================
;; Succeeded

(defmulti charge-succeeded
  (fn [conn charge uuid] (charge/type conn charge)))

(defmethod charge-succeeded :default [_ _ _] :noop)

(defmethod charge-succeeded :security-deposit [conn charge uuid]
  (let [account    (charge/account charge)
        deposit    (deposit/by-charge charge)
        is-initial (deposit/partially-paid? deposit)
        amount     (charge/amount charge)]
    (slack/ops
     (sm/msg
      (sm/success
       (sm/title "Security Deposit Succeeded")
       (sm/text (format "%s's security deposit payment has succeeded."
                        (account/full-name account)))
       (sm/fields
        (sm/field "Payment" (if is-initial "initial" "remainder") true)
        (sm/field "Amount" (format "$%.2f" amount) true))))
     :uuid uuid)))

(defmethod charge-succeeded :rent [conn charge uuid]
  (let [account (charge/account charge)]
    (slack/ops
     (sm/msg
      (sm/success
       (sm/title "Rent Successfully Paid by ACH")
       (sm/text (format "%s's rent ACH payment has succeeded."
                        (account/full-name account)))
       (sm/fields
        (sm/field "Amount" (format "$%.2f" (charge/amount charge)) true))))
     :uuid uuid)))

(defmethod handle msg/charge-succeeded-key
  [conn {charge-id :msg/params :as msg}]
  (let [charge (charge/lookup conn charge-id)]
    (charge-succeeded conn charge (:msg/uuid msg))))

;; =====================================
;; Failed

(defmulti charge-failed
  (fn [conn charge uuid] (charge/type conn charge)))

(defmethod charge-failed :default [_ _ _] :noop)

(defmethod charge-failed :security-deposit [conn charge uuid]
  (let [account      (charge/account charge)
        deposit      (deposit/by-charge charge)
        is-remainder (deposit/partially-paid? deposit)
        amount       (charge/amount charge)]
    (slack/ops
     (sm/msg
      (sm/failure
       (sm/title "Security Deposit ACH Failure")
       (sm/text (format "%s's ACH payment has failed." (account/full-name account)))
       (sm/fields
        (sm/field "Email" (account/email account) true)
        (sm/field "Phone" (account/phone-number account) true)
        (sm/field "Payment" (if is-remainder "remainder" "initial") true)
        (sm/field "Amount" (format "$%.2f" amount) true))))
     :uuid uuid)))

(defmethod charge-failed :rent [conn charge uuid]
  (let [account (charge/account charge)]
    (slack/ops
     (sm/msg
      (sm/failure
       (sm/title "ACH Rent Payment Failed")
       (sm/text (format "%s's rent payment has failed to go through."
                        (account/full-name account)))
       (sm/fields
        (sm/field "Email" (account/email account) true)
        (sm/field "Phone" (account/phone-number account) true))))
     :uuid uuid)))

(defmethod handle msg/charge-failed-key
  [conn {charge-id :msg/params :as msg}]
  (let [charge (charge/lookup conn charge-id)]
    (charge-failed conn charge (:msg/uuid msg))))

;; =============================================================================
;; Customer

(defmulti source-updated
  (fn [conn msg account]
    (get-in msg [:msg/params :status])))

(defmethod source-updated :default
  [conn msg account]
  :noop)

(defmethod source-updated "verification_failed"
  [conn msg account]
  (slack/ops
   (sm/msg
    (sm/failure
     (sm/title "Bank Verification Failed")
     (sm/text (format "%s's bank verification has failed."
                      (account/full-name account)))))
   :uuid (:msg/uuid msg)))

(defmethod source-updated "verified"
  [conn msg account]
  (slack/ops
   (sm/msg
    (sm/success
     (sm/title "Bank Verification Succeeded")
     (sm/text (format "%s has verified their bank account."
                      (account/full-name account)))))
   :uuid (:msg/uuid msg)))

(defmethod handle msg/customer-source-updated-key
  [conn {params :msg/params :as msg}]
  (let [{:keys [customer-id]} params
        account               (account/by-customer-id conn customer-id)]
    (source-updated conn msg account)))

;; =============================================================================
;; Invoice (autopay)

(defn- invoice-dashboard-url [managed-id invoice-id]
  (format "https://dashboard.stripe.com/%s/invoices/%s" managed-id invoice-id))

;; =====================================
;; Payment Failed

(defmethod handle msg/invoice-payment-failed-key
  [conn {invoice-id :msg/params :as msg}]
  (let [payment (rent-payment/by-invoice-id conn invoice-id)
        license (member-license/by-invoice-id conn invoice-id)
        account (member-license/account license)
        managed (member-license/managed-account-id license)]
    (slack/ops
     (sm/msg
      (sm/failure
       (sm/title "View Invoice on Stripe" (invoice-dashboard-url managed invoice-id))
       (sm/text (format "%s's autopay payment has failed" (account/full-name account)))
       (sm/fields
        (sm/field "Attempts" (:rent-payment/autopay-failures payment) true))))
     :uuid (:msg/uuid msg))))

;; =====================================
;; Payment Succeeded

(defmethod handle msg/invoice-payment-succeeded-key
  [conn {invoice-id :msg/params :as msg}]
  (let [license (member-license/by-invoice-id conn invoice-id)
        account (member-license/account license)
        managed (member-license/managed-account-id license)]
    (slack/ops
     (sm/msg
      (sm/success
       (sm/title "View Invoice on Stripe" (invoice-dashboard-url managed invoice-id))
       (sm/text (format "%s's autopay payment has succeeded!"
                        (account/full-name account)))))
     :uuid (:msg/uuid msg))))

;; =============================================================================
;; Notes
;; =============================================================================

(defn- note-url [note]
  (format "%s/admin/accounts/%s/notes" config/hostname (-> note note/account :db/id)))

(defmethod handle msg/note-created-key
  [conn {{:keys [note/uuid notify?]} :msg/params :as msg}]
  (when-let [note (and notify? (note/by-uuid (d/db conn) uuid))]
    (let [ntype (if (note/ticket? note) "Ticket" "Note")]
      (slack/crm
       (sm/msg
        (sm/success
         (sm/title (note/subject note) (note-url note))
         (sm/text (note/content note))
         (sm/fields
          (sm/field "Author" (-> note note/author account/full-name) true)
          (sm/field "Type" (string/lower-case ntype) true))))))))

(defn- notify-handles [note]
  (let [parent-note (note/parent note)
        handles     (->> (conj (note/children parent-note) parent-note)
                         (map (comp account/slack-handle note/author))
                         (remove nil?)
                         (set))]
    (set/difference handles #{(-> note note/author account/slack-handle)})))

(defmethod handle msg/note-comment-created-key
  [conn {{:keys [comment/uuid notify?]} :msg/params :as msg}]
  (when-let [note (and notify? (note/by-uuid (d/db conn) uuid))]
    (doseq [handle (notify-handles note)]
      (slack/send
       {:channel handle}
       (sm/msg
        (sm/info
         (sm/title (format "%s commented on a note that you are subscribed to:"
                           (-> note note/author account/full-name))
                   (note-url (note/parent note)))
         (sm/text (format "_%s_" (note/content note)))))))))

(comment
  (notify-handles
   (note/by-uuid (d/db starcity.datomic/conn) #uuid "58b624a8-4e5d-4b1f-9fc0-ddb5452d31b4"))

  )
