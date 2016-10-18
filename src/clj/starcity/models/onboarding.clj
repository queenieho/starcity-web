(ns starcity.models.onboarding
  (:require [starcity.models.stripe :as stripe]
            [starcity.datomic :refer [conn tempid]]
            [starcity.models.util :refer :all]
            [datomic.api :as d]
            [starcity.spec]
            [clojure.spec :as s]))

;; TODO: If the bank account could not be verified because either of the two small
;; deposits failed, you will receive a customer.source.updated notification. The
;; bank account’s status will be set to verification_failed.
;; https://stripe.com/docs/ach#ach-specific-webhook-notifications

;; TODO: After creating the charge, you will receive a charge.pending
;; notification. You will not receive charge.succeeded or charge.failed
;; notification until up to 5 business days later.

;; =============================================================================
;; Internal
;; =============================================================================

;; =============================================================================
;; DB Lookups

(s/def ::security-deposit
  (s/or
   :nothing empty?
   :security-deposit (s/keys :req [:db/id :security-deposit/account]
                             :opt [:security-deposit/amount-received
                                   :security-deposit/amount-required
                                   :security-deposit/payment-method
                                   :security-deposit/payment-type
                                   :security-deposit/due-by])))

(defn- security-deposit
  "Retrieve the security deposit entity by account id."
  [lookup]
  (ent->map (one (d/db conn) :security-deposit/account lookup)))

(s/fdef security-deposit
        :args (s/cat :lookup :starcity.spec/lookup)
        :ret ::security-deposit)

(s/def :stripe-customer/customer-id string?)

(s/def ::stripe-customer
  (s/or
   :nothing empty?
   :stripe-customer (s/keys :req [:db/id
                                  :stripe-customer/account
                                  :stripe-customer/customer-id]
                            :opt [:stripe-customer/bank-account-token])))

(defn- stripe-customer
  "Retrieve the stripe customer record for given account-id."
  [lookup]
  (ent->map (one (d/db conn) :stripe-customer/account lookup)))

(s/fdef stripe-customer
        :args (s/cat :lookup :starcity.spec/lookup)
        :ret ::stripe-customer)

;; =============================================================================
;; Selectors

(defn- security-deposit-id
  "Retrieve the entity id of the security deposit entity within the `progress` map."
  [progress]
  (get-in progress [:security-deposit :db/id]))

(defn- stripe-customer-id
  "Retrieve the Stripe customer id within the `progress` map."
  [progress]
  (get-in progress [:stripe-customer :stripe-customer/customer-id]))

(def ^:private account-id
  "Retrieve the applicant's account-id from the `progress` map."
  :account-id)

(defn- bank-account-token
  "Retrieve the bank account token within the `progress` map."
  [progress]
  (get-in progress [:stripe-customer :stripe-customer/bank-account-token]))

;; =============================================================================
;; Transactions

(declare monthly-rent)

(defn- create-security-deposit
  [progress payment-method]
  (let [amount-required (int (* 100 (monthly-rent progress)))]
    @(d/transact conn [{:db/id                            (tempid)
                        :security-deposit/account         (account-id progress)
                        :security-deposit/payment-method  payment-method
                        :security-deposit/amount-required amount-required}])))

(defn- update-payment-method*
  [security-deposit-id payment-method]
  @(d/transact conn [{:db/id                           security-deposit-id
                      :security-deposit/payment-method payment-method}]))

;; =============================================================================
;; API
;; =============================================================================

;; =============================================================================
;; Specs

(s/def ::acount-id integer?)

(s/def ::payment-method
  #{:security-deposit.payment-method/ach
    :security-deposit.payment-method/check})

(s/def ::progress
  (s/keys :req-un [::account-id ::security-deposit ::stripe-customer]))

;; =============================================================================
;; Selectors

(defn payment-method
  "Get the payment method from the onboarding progress."
  [progress]
  (get-in progress [:security-deposit :security-deposit/payment-method]))

(s/fdef payment-method
        :args (s/cat :progress ::progress))

(defn monthly-rent
  "Produce the monthly rent that this applicant has agreed to pay."
  [progress]
  (-> (d/q '[:find ?price
             :in $ ?account
             :where
             ;; Get the desired license via account application
             [?account :account/member-application ?application]
             [?application :member-application/desired-license ?desired-license]
             ;; Approval is where we get the property from
             [?approval :approval/account ?account]
             [?approval :approval/property ?property]
             ;; Get the property-license that matches the desired license
             [?property :property/licenses ?license]
             [?license :property-license/license ?desired-license]
             ;; Finally, pull out the prrice
             [?license :property-license/base-price ?price]]
           (d/db conn) (:account-id progress))
      ffirst))

(defn applicant-property
  "The property that this applicant is being onboarded for."
  [progress]
  (:approval/property
   (one (d/db conn) :approval/account (account-id progress))))

(def property-code
  (comp :property/internal-name applicant-property))

;; =============================================================================
;; Transactions

(defn update-payment-method
  "Update the payment method. Will create a new security deposit entity if one
  does not already exist, or update the existing one if it does."
  [progress method]
  (if-let [sid (security-deposit-id progress)]
    (update-payment-method* sid method)
    (create-security-deposit progress method)))

;; =====================================
;; Perform ACH charge

;; Firstly, we need to know the correct amount to charge.
;; We can determine this by using a map of {#{"full" "partial"} <amt>}


(defn- charge-amount
  "Determine the correct amount to charge in cents given "
  [progress payment-choice]
  (assert (#{"full" "partial"} payment-choice)
          (format "Payment choice must be one of #{\"full\" \"partial\"}, not %s."
                  payment-choice))
  (if (= "full" payment-choice)
    (int (* 100 (monthly-rent progress)))
    50000))

;; Now that we know how much to charge, we can actually make the charge!
;; If it succeeds, we'll want to create/update entities in the DB to reflect the
;; success. If not, an error will propogate up from the `stripe' model.

(defn- create-ach-charge
  [progress payment-choice]
  (stripe/create-charge (account-id progress)
                        (charge-amount progress payment-choice)
                        (bank-account-token progress)
                        :description (format "'%s' security deposit payment" payment-choice)
                        :customer-id (stripe-customer-id progress)
                        :managed-account (-> progress applicant-property :property/managed-account-id)))

;; Assuming no error, we now need to update the security deposit entity to
;; reflect the successful transaction.

(defn- record-security-deposit-payment
  "Update the `security-deposit` entity to reflect that the security deposit has
  been paid via a successful ACH payment."
  [progress payment-choice charge-id]
  (let [payment-type (keyword "security-deposit.payment-type" payment-choice)]
    @(d/transact conn [{:db/id                            (security-deposit-id progress)
                        :security-deposit/amount-received (charge-amount progress payment-choice)
                        :security-deposit/charge          charge-id
                        :security-deposit/payment-type    payment-type}])))

;; Here's what ties it all together.

(declare payment-received?)

;; TODO: Ensure that there's no payment already created. Don't want the
;; possibility of multiple payments
(defn pay-ach
  "Pay the security deposit with ACH given current progress and the choice of
  payment method."
  [progress payment-choice]
  (if (payment-received? progress)
    (throw (ex-info "Cannot charge customer for security deposit twice!" progress))
    (let [charge-id (create-ach-charge progress payment-choice)]
     (record-security-deposit-payment progress payment-choice charge-id))))

(s/fdef pay-ach
        :args (s/cat :progress ::progress
                     :payment-choice #{"full" "partial"}))

;; =============================================================================
;; Predicates

(defn payment-method-chosen?
  "Has a payment method been chosen?"
  [progress]
  (let [{:keys [:security-deposit/payment-method]} (:security-deposit progress)]
    (#{:security-deposit.payment-method/ach
       :security-deposit.payment-method/check}
     payment-method)))

(defn customer-created?
  "Has a Stripe customer been created?"
  [progress]
  (-> progress :stripe-customer empty? not))

(defn bank-account-verified?
  "Is the stripe customer's bank account verified?"
  [progress]
  (or (:stripe-customer/bank-account-token progress) ; Only present in DB after
                                        ; verification
      (and (stripe-customer-id progress)
           (stripe/bank-account-verified?
            (stripe/fetch-customer (stripe-customer-id progress))))))

(defn payment-received?
  "Has payment already been received?"
  [progress]
  (and (payment-method-chosen? progress)
       (> (get-in progress [:security-deposit :security-deposit/amount-received] 0) 0)))

;; =============================================================================
;; Get Progress

(defn get-progress
  [lookup]
  {:account-id       lookup
   :stripe-customer  (stripe-customer lookup)
   :security-deposit (security-deposit lookup)})

(s/fdef get-progress
        :args (s/cat :lookup :starcity.spec/lookup)
        :ret ::progress)

(comment

  (get-progress [:account/email "onboarding@test.com"])

  )
