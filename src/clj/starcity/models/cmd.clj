(ns starcity.models.cmd
  (:require [clojure.spec :as s]
            [datomic.api :as d]
            [plumbing.core :refer [assoc-when]]
            [starcity.datomic :refer [tempid]]
            [taoensso.nippy :as nippy]
            [toolbelt.predicates :as p]))

;; =============================================================================
;; Spec
;; =============================================================================

(s/def :cmd/key keyword?)
(s/def :cmd/uuid uuid?)
(s/def :cmd/params bytes?)
(s/def :cmd/id string?)
(s/def :cmd/status
  #{:cmd.status/pending
    :cmd.status/successful
    :cmd.status/failed})
(s/def ::cmd
  (s/keys :req [:db/id
                :cmd/uuid
                :cmd/key
                :cmd/status]
          :opt [:cmd/id
                :cmd/params]))

;; =============================================================================
;; Transactions
;; =============================================================================

(defn create
  "Create a new cmd."
  [cmd-type & {:keys [params cmd-id meta]}]
  (let [meta (when meta (nippy/freeze meta))
        data (assoc-when
              {:db/id      (tempid)
               :cmd/uuid   (d/squuid)
               :cmd/key    cmd-type
               :cmd/status :cmd.status/pending}
              :cmd/id cmd-id
              :cmd/meta meta)]
    (if-not params
      data
      (assoc data :cmd/params (nippy/freeze params)))))

(s/def ::cmd-id :cmd/id)
(s/fdef create
        :args (s/cat :key keyword?
                     :opts (s/keys* :opt-un [::params ::cmd-id ::meta]))
        :ret ::cmd)

(defn successful [cmd]
  {:db/id      (:db/id cmd)
   :cmd/status :cmd.status/successful})

(defn failed [cmd]
  {:db/id      (:db/id cmd)
   :cmd/status :cmd.status/failed})

(defn retry
  "An cmd will be effectively retried if its status goes back to pending."
  [cmd]
  {:db/id      (:db/id cmd)
   :cmd/status :cmd.status/pending})

;; =============================================================================
;; Named
;; =============================================================================

;; =============================================================================
;; Accounts

(def create-account-key :account/create)

(defn create-account
  "A new account should be created."
  [email password first-name last-name]
  (create create-account-key :params {:email      email
                                      :password   password
                                      :first-name first-name
                                      :last-name  last-name}))

(s/fdef create-account
        :args (s/cat :email string? :password string? :first-name string? :last-name string?)
        :ret ::cmd)

;; =============================================================================
;; Collaborators

(def add-collaborator-key :collaborator/add)

(defn add-collaborator
  "A collaborator has indicated that he/she is interested in getting touch."
  [email type message]
  (create add-collaborator-key :params {:email   email
                                        :type    type
                                        :message message}))

(s/fdef add-collaborator
        :args (s/cat :email string?
                     :type #{"real-estate" "community-stakeholder" "vendor" "investor"}
                     :message string?)
        :ret ::cmd)

;; =============================================================================
;; Newsletter

(def subscribe-to-newsletter-key :newsletter/subscribe)

(defn subscribe-to-newsletter
  "`email` should be subscribed to our newsletter."
  [email]
  (create subscribe-to-newsletter-key :params {:email email}))

(s/fdef subscribe-to-newsletter
        :args (s/cat :email string?)
        :ret ::cmd)

;; =============================================================================
;; Rent

(def create-rent-payments-key :rent.payments/create)

(defn create-rent-payments
  "New rent payments should be created for `time-period`."
  [time-period]
  (create create-rent-payments-key :params time-period))

(s/fdef create-rent-payments
        :args (s/cat :time-period inst?)
        :ret ::cmd)

;; =============================================================================
;; Stripe

(def stripe-webhook-event-key :stripe/event)

(defn stripe-webhook-event
  "Stripe sends us events via a webhook that often need to be processed."
  [event-id event-type connect-id]
  (if connect-id
    (create stripe-webhook-event-key
            :params {:event-type event-type}
            :meta {:managed-account-id connect-id}
            :cmd-id event-id)
    (create stripe-webhook-event-key
            :params {:event-type event-type}
            :cmd-id event-id)))

(s/fdef stripe-webhook-event
        :args (s/cat :event-id string?
                     :event-type string?
                     :connected-account (s/or :nothing nil? :string string?))
        :ret ::cmd)

(def delete-customer-key :stripe.customer/delete)

(defn delete-customer
  "Delete a customer on Stripe."
  [stripe-customer]
  (create delete-customer-key :params (:db/id stripe-customer)))

(s/fdef delete-customer
        :args (s/cat :stripe-customer p/entity?)
        :ret ::cmd)

;; =============================================================================
;; Session

(def delete-session-key :session/delete)

(defn delete-session
  "Delete `account`'s session."
  [account]
  (create delete-session-key :params (:db/id account)))

(s/fdef delete-session
        :args (s/cat :account p/entity?)
        :ret ::cmd)
