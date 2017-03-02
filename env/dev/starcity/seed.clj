(ns starcity.seed
  (:require [clj-time
             [coerce :as c]
             [core :as t]]
            [datomic.api :as d]
            [io.rkn.conformity :as cf]
            [plumbing.core :refer [assoc-when]]
            [starcity-db.core :refer [part]]
            [starcity.datomic :refer [conn]]
            [starcity.models
             [account :as account]
             [check :as check]
             [member-license :as member-license]
             [property :as property]
             [rent-payment :as rp]]))

;; =============================================================================
;; Internal
;; =============================================================================

;; =============================================================================
;; Helpers

(defn license [conn term]
  (d/q '[:find ?e .
         :in $ ?term
         :where [?e :license/term ?term]]
       (d/db conn) term))

;; =============================================================================
;; Accounts

(def password
  "bcrypt+blake2b-512$30e1776f40ee533841fcba62a0dbd580$12$2dae523ec1eb9fd91409ebb5ed805fe53e667eaff0333243")

(defn account
  [email first-name last-name phone role & [slack-handle]]
  (assoc-when
   {:db/id                (d/tempid part)
    :account/email        email
    :account/password     password
    :account/first-name   first-name
    :account/last-name    last-name
    :account/phone-number phone
    :account/role         role
    :account/activated    true}
   :account/slack-handle slack-handle))

(def accounts-tx
  [(account "test@test.com" "Applicant" "User" "2345678910" :account.role/applicant)
   (account "applicant@test.com" "Applicant" "User" "2345678910" :account.role/applicant)
   (account "member@test.com" "Member" "User" "2345678910" :account.role/member)
   (account "onboarding@test.com" "Onboarding" "User" "2345678910" :account.role/onboarding)
   (account "admin@test.com" "Admin" "User" "2345678910" :account.role/admin)
   (account "josh@joinstarcity.com" "Josh" "Lehman" "2345678910" :account.role/admin "@josh")
   ;; Testing Accounts
   (account "jon@test.com" "Jon" "Dishotsky" "2345678910" :account.role/onboarding)
   (account "jesse@test.com" "Jesse" "Suarez" "2345678910" :account.role/onboarding)
   (account "mo@test.com" "Mo" "Sakrani" "2345678910" :account.role/onboarding)
   (account "meg@test.com" "Meg" "Bell" "2345678910" :account.role/onboarding)
   (account "jp@test.com" "Josh" "Petersen" "2345678910" :account.role/onboarding)])

;; =============================================================================
;; Licenses

(def licenses-tx
  [{:db/id (d/tempid part) :license/term 1 :license/available false}
   {:db/id (d/tempid part) :license/term 3}
   {:db/id (d/tempid part) :license/term 6 :license/available true}
   {:db/id (d/tempid part) :license/term 12}])

;; =============================================================================
;; Properties

(defn property-licenses [conn & ls]
  (map
   (fn [[term price]]
     {:property-license/license    (license conn term)
      :property-license/base-price price})
   ls))

(defn address [lines]
  {:address/lines lines
   :address/city  "San Francisco"})

(defn units [property-name n]
  (for [i (range n)]
    {:unit/name (format "%s-%s" property-name (inc i))}))

(defn property
  [name internal-name available-on address licenses units
   & {:keys [managed-account-id ops-fee]}]
  (assoc-when
   {:db/id                  (d/tempid part)
    :property/name          name
    :property/internal-name internal-name
    :property/available-on  available-on
    :property/licenses      licenses
    :property/units         units}
   :property/managed-account-id managed-account-id
   :property/ops-fee ops-fee))

(defn properties-tx [conn]
  (let [licenses (partial property-licenses conn)]
    [(property "West SoMa"
               "52gilbert"
               #inst "2016-12-01T00:00:00.000-00:00"
               (address "52 Gilbert St.")
               (licenses [1 2300.0] [3 2300.0] [6 2100.0] [12 2000.0])
               (units "52gilbert" 6)
               :managed-account-id "acct_191838JDow24Tc1a"
               :ops-fee 28.0)
     (property "The Mission"
               "2072mission"
               #inst "2017-01-01T00:00:00.000-00:00"
               (address "2072 Mission St.")
               (licenses [1 2400.0] [3 2400.0] [6 2200.0] [12 2100.0])
               (units "2072mission" 17))]))

;; =============================================================================
;; Approval

(def approval-tx
  [{:db/id                (d/tempid part)
    :approval/account     [:account/email "onboarding@test.com"]
    :approval/approved-by [:account/email "admin@test.com"]
    :approval/approved-on (java.util.Date.)
    :approval/property    [:property/internal-name "52gilbert"]}
   {:db/id                            (d/tempid part)
    :security-deposit/account         [:account/email "onboarding@test.com"]
    :security-deposit/amount-required 2100}])

;; =============================================================================
;; Applications

(defn application
  [account-id & {:keys [address properties license move-in pet fitness status]
                 :or   {move-in (c/to-date (t/plus (t/now) (t/weeks 2)))
                        status  :application.status/in-progress}}]
  (let [id (d/tempid part)]
    [{:db/id               account-id
      :account/application id}
     (assoc-when {:db/id              id
                  :application/status status}
                 :application/license license
                 :application/communities properties
                 :application/address address
                 :application/move-in move-in
                 :application/has-pet (boolean pet)
                 :application/fitness fitness)]))

(defn applications-tx [conn]
  (concat
   (application [:account/email "test@test.com"]
                :license (license conn 3))
   (application [:account/email "applicant@test.com"]
                :address {:address/country     "US"
                          :address/region      "CA"
                          :address/locality    "Oakland"
                          :address/postal-code "94611"}
                :license (license conn 3)
                :status :application.status/submitted
                :properties [[:property/internal-name "52gilbert"]
                             [:property/internal-name "2072mission"]]
                :move-in (c/to-date (t/date-time 2017 4 1))
                :fitness {:fitness/experience   "Donec neque quam, dignissim in, mollis nec, sagittis eu, wisi."
                          :fitness/skills       "Donec neque quam, dignissim in, mollis nec, sagittis eu, wisi."
                          :fitness/free-time    "Donec neque quam, dignissim in, mollis nec, sagittis eu, wisi."
                          :fitness/interested   "Donec neque quam, dignissim in, mollis nec, sagittis eu, wisi."
                          :fitness/dealbreakers "Donec neque quam, dignissim in, mollis nec, sagittis eu, wisi."})
   (application [:account/email "onboarding@test.com"]
                :license (license conn 6)
                :status :application.status/approved
                :properties [[:property/internal-name "52gilbert"]])))

;; =============================================================================
;; Security Deposits

(defn- create-security-deposit [account]
  {:db/id                            (d/tempid part)
   :security-deposit/account         account
   :security-deposit/amount-required 2000
   :security-deposit/amount-received 500
   :security-deposit/payment-method  :security-deposit.payment-method/ach
   :security-deposit/payment-type    :security-deposit.payment-type/partial})

(def security-deposits-tx
  (map
   (comp create-security-deposit (partial conj [:account/email]))
   ["member@test.com"
    "jon@test.com"
    "jesse@test.com"
    "mo@test.com"
    "meg@test.com"
    "jp@test.com"]))

;; =============================================================================
;; Member Licenses

(defn member-licenses-tx [conn]
  (let [member  (d/entity (d/db conn) [:account/email "member@test.com"])
        unit    (->> (d/entity (d/db conn) [:property/internal-name "52gilbert"])
                     (property/available-units conn)
                     first)
        license (->> (d/q '[:find ?e . :where [?e :license/term 3]] (d/db conn))
                     (d/entity (d/db conn)))]
    (account/promote-to-member conn member license unit (java.util.Date.) 2000.0)))

;; =============================================================================
;; Rent Payments

(defn- date [y m d]
  (c/to-date (t/date-time y m d)))

(def check-december
  (let [start (date 2016 12 1)
        end   (date 2016 12 31)]
    (rp/create 2000.0 start end :rent-payment.status/paid
               :method rp/check
               :check (check/create "Member" 2000.0 (java.util.Date.) 1175)
               :due-date (date 2016 12 5)
               :paid-on (date 2016 12 15))))

#_(def check-november-partial
    (let [start (date 2016 11 15)
          end   (date 2016 11 30)]
      (rp/create 1000.0 start end :rent-payment.status/paid
                 :method rp/check
                 :check (check/create "Member" 2000.0 (java.util.Date.) 1174)
                 :due-date (date 2016 11 20)
                 :paid-on (date 2016 11 19))))

(def check-november-other
  (rp/create 1000.0 (date 2016 11 15) (date 2016 11 30) :rent-payment.status/paid
             :method rp/other
             :due-date (date 2016 11 20)
             :paid-on (date 2016 11 19)
             :desc "bill.com"))

(defn- rent-payments-tx [conn]
  (let [license (->> (d/entity (d/db conn) [:account/email "member@test.com"])
                     (member-license/active conn))]
    [(member-license/add-rent-payments
      license
      check-december
      check-november-other)]))

;; =============================================================================
;; Stripe Customers

(def stripe-customers-tx
  [{:db/id                              (d/tempid part)
    :stripe-customer/account            [:account/email "member@test.com"]
    :stripe-customer/customer-id        "cus_9bzpu7sapb8g7y"
    :stripe-customer/bank-account-token "ba_19IlpVIvRccmW9nO20kCxqE5"}])

;; =============================================================================
;; Avatar

(def avatar-tx
  [{:db/id       (d/tempid part)
    :avatar/name :system
    :avatar/url  "/assets/img/starcity-logo-black.png"}])

;; =============================================================================
;; API
;; =============================================================================

(defn seed
  "Seed the database with sample data."
  [conn]
  (cf/ensure-conforms conn {:seed/licenses {:txes [licenses-tx]}})
  (cf/ensure-conforms
   conn
   {:seed/accounts          {:txes [accounts-tx]}
    :seed/licenses          {:txes [licenses-tx]}
    :seed/properties        {:txes     [(properties-tx conn)]
                             :requires [:seed/licenses]}
    :seed/approval          {:txes     [approval-tx]
                             :requires [:seed/accounts
                                        :seed/properties]}
    :seed/applications      {:txes     [(applications-tx conn)]
                             :requires [:seed/accounts
                                        :seed/licenses
                                        :seed/properties]}
    :seed/security-deposits {:txes     [security-deposits-tx]
                             :requires [:seed/accounts]}
    :seed/stripe-customers  {:txes     [stripe-customers-tx]
                             :requires [:seed/accounts]}
    :seed/avatar            {:txes [avatar-tx]}})

  ;; NOTE: These need to happen in separate transactions.
  (cf/ensure-conforms
   conn
   {:seed/member-licenses {:txes [(member-licenses-tx conn)]}})
  (cf/ensure-conforms
   conn
   {:seed/rent-payments {:txes [(rent-payments-tx conn)]}}) )


(comment
  (let [license-id (d/q '[:find ?l .
                          :in $ ?a
                          :where
                          [?a :account/licenses ?l]
                          [?l :member-license/status :member-license.status/active]]
                        (d/db conn) [:account/email "member@test.com"])]
    (d/transact conn [{:db/id license-id
                       :member-license/plan-id "285873023222909"
                       :member-license/subscription-id "sub_9zbG4ycfe4VA1u"}]))

  )
