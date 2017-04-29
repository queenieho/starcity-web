(ns starcity.seed
  (:require [clj-time
             [coerce :as c]
             [core :as t]]
            [datomic.api :as d]
            [io.rkn.conformity :as cf]
            [plumbing.core :refer [assoc-when]]
            [starcity.datomic :refer [conn tempid]]
            [starcity.models
             [account :as account]
             [application :as app]
             [approval :as approval]
             [check :as check]
             [license :as license]
             [member-license :as member-license]
             [property :as property]
             [rent-payment :as rp]
             [security-deposit :as deposit]
             [unit :as unit]]
            [starcity.models.onboard :as onboard]
            [starcity.models.service :as service]))

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
   {:db/id                (tempid)
    :account/email        email
    :account/password     password
    :account/first-name   first-name
    :account/last-name    last-name
    :account/phone-number phone
    :account/role         role
    :account/activated    true}
   :account/slack-handle slack-handle))

(defn accounts-tx []
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

(defn licenses-tx []
  [{:db/id (tempid) :license/term 1 :license/available false}
   {:db/id (tempid) :license/term 3}
   {:db/id (tempid) :license/term 6 :license/available true}
   {:db/id (tempid) :license/term 12}])

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
   & {:keys [managed-account-id ops-fee tours]
      :or   {tours false}}]
  (assoc-when
   {:db/id                  (tempid)
    :property/name          name
    :property/internal-name internal-name
    :property/available-on  available-on
    :property/licenses      licenses
    :property/units         units
    :property/tours         tours}
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
               (units "2072mission" 17)
               :tours true)]))

;; =============================================================================
;; Approval

(defn approve
  "A more light-weight version of `starcity.models.approval/approve` that
  doesn't create `msg` and `cmd`."
  [approver approvee unit license move-in]
  [(approval/create approver approvee unit license move-in)
   ;; Change role
   {:db/id (:db/id approvee) :account/role :account.role/onboarding}
   (deposit/create approvee (int (unit/rate unit license)))
   (onboard/create approvee)
   (app/change-status (:account/application approvee)
                      :application.status/approved)])

(defn approval-tx [conn]
  (concat
   (approve
    (d/entity (d/db conn) [:account/email "admin@test.com"])
    (d/entity (d/db conn) [:account/email "member@test.com"])
    (unit/by-name (d/db conn) "52gilbert-1")
    (license/by-term conn 3)
    (c/to-date (t/now)))
   (approve
    (d/entity (d/db conn) [:account/email "admin@test.com"])
    (d/entity (d/db conn) [:account/email "onboarding@test.com"])
    (unit/by-name (d/db conn) "2072mission-10")
    (license/by-term conn 3)
    (c/to-date (t/plus (t/now) (t/months 1))))))

;; =============================================================================
;; Applications

(defn application
  [account-id & {:keys [address properties license move-in pet fitness status]
                 :or   {move-in (c/to-date (t/plus (t/now) (t/weeks 2)))
                        status  :application.status/in-progress}}]
  (let [id (tempid)]
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
                :properties [[:property/internal-name "52gilbert"]])
   (application [:account/email "member@test.com"]
                :license (license conn 3)
                :status :application.status/approved
                :properties [[:property/internal-name "52gilbert"]])))

;; =============================================================================
;; Security Deposits

(defn- create-security-deposit [account]
  {:db/id                            (tempid)
   :security-deposit/account         account
   :security-deposit/amount-required 2000
   :security-deposit/amount-received 500
   :security-deposit/payment-method  :security-deposit.payment-method/ach
   :security-deposit/payment-type    :security-deposit.payment-type/partial})

(defn security-deposits-tx []
  (map
   (comp create-security-deposit (partial conj [:account/email]))
   ["jon@test.com"
    "jesse@test.com"
    "mo@test.com"
    "meg@test.com"
    "jp@test.com"]))

;; =============================================================================
;; Member Licenses

(defn member-licenses-tx [conn]
  (let [admin  (d/entity (d/db conn) [:account/email "admin@test.com"])
        member (d/entity (d/db conn) [:account/email "member@test.com"])]
    (remove #(contains? % :msg/uuid) (account/promote member))))

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

(defn stripe-customers-tx []
  [{:db/id                              (tempid)
    :stripe-customer/account            [:account/email "member@test.com"]
    :stripe-customer/customer-id        "cus_9bzpu7sapb8g7y"
    :stripe-customer/bank-account-token "ba_19IlpVIvRccmW9nO20kCxqE5"}])

;; =============================================================================
;; Avatar

(defn avatar-tx []
  [{:db/id       (tempid)
    :avatar/name :system
    :avatar/url  "/assets/img/starcity-logo-black.png"}])

;; =============================================================================
;; Services

(defn services-tx []
  [{:db/id          (tempid)
    :service/code   "storage,bin,small"
    :service/name   "Small Storage Bin"
    :service/desc   "An 18 gallon bin for your belongings."
    :service/price  6.0
    :service/billed :service.billed/monthly}
   {:db/id          (tempid)
    :service/code   "storage,bin,large"
    :service/name   "Large Storage Bin"
    :service/desc   "A 30 gallon bin for your belongings."
    :service/price  8.0
    :service/billed :service.billed/monthly}
   {:db/id          (tempid)
    :service/code   "storage,misc"
    :service/name   "Other Storage"
    :service/desc   "Storage for large/oddly-shaped items that don't fit in bins (e.g. musical instruments, sports equipment, etc)."
    :service/billed :service.billed/monthly}
   {:db/id          (tempid)
    :service/code   "moving,move-in"
    :service/name   "Move-in Assistance"
    :service/desc   "Movers to help move your things into your room on move-in day."
    :service/price  50.0
    :service/billed :service.billed/once}
   {:db/id          (tempid)
    :service/code   "customize,furniture,quote"
    :service/name   "Furniture/Layout Customization"
    :service/desc   "request for a quote on arbitrary furniture-related modifications"
    :service/billed :service.billed/once}
   {:db/id          (tempid)
    :service/code   "customize,room,quote"
    :service/name   "Room Personalization"
    :service/desc   "request for a quote on arbitrary room modifications"
    :service/billed :service.billed/once}
   {:db/id          (tempid)
    :service/code   "cleaning,weekly"
    :service/name   "Weekly Room Cleaning"
    :service/desc   "Have your room dusted, vacuumed and all surfaces cleaned on a weekly basis to keep things fresh and tidy."
    :service/price  100.0
    :service/billed :service.billed/monthly}
   {:db/id          (tempid)
    :service/code   "laundry,weekly"
    :service/name   "Complete Laundry Service &amp; Delivery"
    :service/desc   "Give us your dirty laundry, we'll bring it back so fresh and so clean. Whether you need dry-cleaning or wash and fold, we'll keep your shirts pressed and your jackets stain-free with our next-day laundry service. The membership price includes pickup and delivery&mdash;individual item pricing will be billed at the end of the month."
    :service/price  40.0
    :service/billed :service.billed/monthly}
   {:db/id              (tempid)
    :service/code       "kitchenette,coffee/tea,bundle"
    :service/name       "Coffee or Tea Equipment Bundle"
    :service/desc       "Includes coffee/tea maker of your choice, electric water kettle, mugs and tray."
    :service/properties [[:property/internal-name "2072mission"]]
    :service/price      75.0
    :service/billed     :service.billed/once
    :service/variants   [{:svc-variant/name "Chemex"}
                         {:svc-variant/name "French Press"}
                         {:svc-variant/name "Tea Infuser Pot"}]}
   {:db/id              (tempid)
    :service/code       "kitchenette,microwave"
    :service/name       "Microwave"
    :service/desc       "A microwave for your kitchenette, rented for the duration of your membership."
    :service/price      50.0
    :service/rental     true
    :service/properties [[:property/internal-name "2072mission"]]
    :service/billed     :service.billed/once}
   {:db/id          (tempid)
    :service/code   "mirror,full-length"
    :service/name   "Full-length Mirror"
    :service/desc   "Rent a full-length mirror for the duration of your membership."
    :service/rental true
    :service/price  25.0
    :service/billed :service.billed/once}
   {:db/id          (tempid)
    :service/code   "tv,wall,32inch"
    :service/name   "Wall-mounted 32\" TV"
    :service/desc   "Includes the TV, wall mount and installation."
    :service/price  315.0
    :service/billed :service.billed/once}
   {:db/id          (tempid)
    :service/code   "apple-tv"
    :service/name   "Apple TV"
    :service/desc   "Apple TV and installation."
    :service/price  155.0
    :service/billed :service.billed/once}
   {:db/id          (tempid)
    :service/code   "box-fan"
    :service/name   "Box Fan"
    :service/desc   "A box fan for warm summer days."
    :service/price  25.0
    :service/rental true
    :service/billed :service.billed/once}
   {:db/id          (tempid)
    :service/code   "white-noise-machine"
    :service/name   "White Noise Machine"
    :service/desc   "Tune out the city while you sleep."
    :service/price  50.0
    :service/rental true
    :service/billed :service.billed/once}
   {:db/id            (tempid)
    :service/code     "plants,planter"
    :service/name     "Planter of Plants"
    :service/desc     "Add a little life to your room."
    :service/billed   :service.billed/once
    :service/variants [{:svc-variant/name  "small"
                        :svc-variant/price 25.0}
                       {:svc-variant/name  "medium"
                        :svc-variant/price 40.0}
                       {:svc-variant/name  "hanging"
                        :svc-variant/price 40.0}]}])

(defn catalogues [conn]
  (let [db   (d/db conn)
        soma (d/entity db [:property/internal-name "52gilbert"])
        miss (d/entity db [:property/internal-name "2072mission"])]
    [;; Storage Options, SoMa
     {:db/id                (tempid)
      :catalogue/name       "Storage Options, 52 Gilbert"
      :catalogue/code       :storage
      :catalogue/properties (:db/id soma)
      :catalogue/items
      [{:cat-item/index   0
        :cat-item/service (:db/id (service/by-code db "storage,bin,small"))
        :cat-item/fields
        {:cat-field/label "How many?"
         :cat-field/type  :cat-field.type/quantity
         :cat-field/min   1
         :cat-field/max   10
         :cat-field/step  1.0}}
       {:cat-item/index   1
        :cat-item/service (:db/id (service/by-code db "storage,bin,large"))
        :cat-item/fields
        {:cat-field/label "How many?"
         :cat-field/type  :cat-field.type/quantity
         :cat-field/min   1
         :cat-field/max   10
         :cat-field/step  1.0}}
       {:cat-item/index   2
        :cat-item/service (:db/id (service/by-code db "storage,misc"))
        :cat-item/fields
        {:cat-field/label "Tell us about what you'd like to store and we'll get back to you with a quote within 24 hours."
         :cat-field/type  :cat-field.type/desc}}]}
     ;; Storage Options, Mission
     {:db/id                (tempid)
      :catalogue/name       "Storage Options, 2072 Mission"
      :catalogue/code       :storage
      :catalogue/properties (:db/id miss)
      :catalogue/items
      [{:cat-item/index   0
        :cat-item/service (:db/id (service/by-code db "storage,bin,small"))
        :cat-item/fields
        {:cat-field/label "How many?"
         :cat-field/type  :cat-field.type/quantity
         :cat-field/min   1
         :cat-field/max   10
         :cat-field/step  1.0}}
       {:cat-item/index   1
        :cat-item/service (:db/id (service/by-code db "storage,bin,large"))
        :cat-item/fields
        {:cat-field/label "How many?"
         :cat-field/type  :cat-field.type/quantity
         :cat-field/min   1
         :cat-field/max   10
         :cat-field/step  1.0}}
       {:cat-item/index   2
        :cat-item/service (:db/id (service/by-code db "storage,misc"))
        :cat-item/fields
        {:cat-field/label "Tell us about what you'd like to store and we'll get back to you with a quote within 24 hours."
         :cat-field/type  :cat-field.type/desc}}]}
     ;; Room Customization
     {:db/id          (tempid)
      :catalogue/name "Room Customization"
      :catalogue/code :room/customize
      :catalogue/items
      [{:cat-item/index   0
        :cat-item/service (:db/id (service/by-code db "customize,furniture,quote"))
        :cat-item/desc    "Would you like to customize your room layout, furniture arrangement, or request a specific furniture item?"
        :cat-item/fields
        {:cat-field/label "Please leave a detailed description about what you'd like to accomplish and we'll get you a quote within 24 hours."
         :cat-field/type  :cat-field.type/desc}}
       {:cat-item/index   1
        :cat-item/service (:db/id (service/by-code db "customize,room,quote"))
        :cat-item/desc    "Your room comes fully furnished and decorated. Please let us know if you would like to personalize the room to reflect your personality and taste. From painting accent colors, and curating art from local artists to hanging/framing your own art, we've got you covered."
        :cat-item/fields
        {:cat-field/label "Please leave a detailed description about what you'd like to accomplish and we'll get you a quote within 24 hours."
         :cat-field/type  :cat-field.type/desc}}]}
     ;; Cleaning & Laundry
     {:db/id          (tempid)
      :catalogue/name "Cleaning and Laundry"
      :catalogue/code :cleaning+laundry
      :catalogue/items
      [{:cat-item/index   0
        :cat-item/service (:db/id (service/by-code db "cleaning,weekly"))}
       {:cat-item/index   1
        :cat-item/service (:db/id (service/by-code db "laundry,weekly"))}]}
     ;; Room Upgrades, Mission
     {:db/id                (tempid)
      :catalogue/name       "Room Upgrades"
      :catalogue/properties [[:property/internal-name "2072mission"]]
      :catalogue/code       :room/upgrades
      :catalogue/items
      (map-indexed
       #(assoc %2 :cat-item/index %1)
       [{:cat-item/service (:db/id (service/by-code db "kitchenette,coffee/tea,bundle"))}
        {:cat-item/service (:db/id (service/by-code db "kitchenette,microwave"))}
        {:cat-item/service (:db/id (service/by-code db "mirror,full-length"))}
        {:cat-item/service (:db/id (service/by-code db "tv,wall,32inch"))}
        {:cat-item/service (:db/id (service/by-code db "apple-tv"))}
        {:cat-item/service (:db/id (service/by-code db "box-fan"))}
        {:cat-item/service (:db/id (service/by-code db "white-noise-machine"))}
        {:cat-item/name    "Planter of Plants"
         :cat-item/desc    "Add a little life to your room."
         :cat-item/service (:db/id (service/by-code db "plants,planter"))}])}
     ;; Room Upgrades, SoMa
     {:db/id                (tempid)
      :catalogue/name       "Room Upgrades"
      :catalogue/properties [[:property/internal-name "52gilbert"]]
      :catalogue/code       :room/upgrades
      :catalogue/items
      [{:cat-item/index   0
        :cat-item/service (:db/id (service/by-code db "mirror,full-length"))}
       {:cat-item/index   1
        :cat-item/service (:db/id (service/by-code db "tv,wall,32inch"))}
       {:cat-item/index   2
        :cat-item/service (:db/id (service/by-code db "apple-tv"))}
       {:cat-item/index   3
        :cat-item/service (:db/id (service/by-code db "box-fan"))}
       {:cat-item/index   4
        :cat-item/service (:db/id (service/by-code db "white-noise-machine"))}
       {:cat-item/index   5
        :cat-item/name    "Planter of Plants"
        :cat-item/desc    "Add a little life to your room."
        :cat-item/service (:db/id (service/by-code db "plants,planter"))}]}]))

;; =============================================================================
;; API
;; =============================================================================

(defn seed
  "Seed the database with sample data."
  [conn]
  (cf/ensure-conforms
   conn
   {:seed/accounts          {:txes [(accounts-tx)]}
    :seed/licenses          {:txes [(licenses-tx)]}
    :seed/properties        {:txes     [(properties-tx conn)]
                             :requires [:seed/licenses]}
    :seed/applications      {:txes     [(applications-tx conn)]
                             :requires [:seed/accounts
                                        :seed/licenses
                                        :seed/properties]}
    :seed/security-deposits {:txes     [(security-deposits-tx)]
                             :requires [:seed/accounts]}
    :seed/services          {:txes     [(services-tx)]
                             :requires [:seed/properties]}
    :seed/stripe-customers  {:txes     [(stripe-customers-tx)]
                             :requires [:seed/accounts]}
    :seed/avatar            {:txes [(avatar-tx)]}})
  ;; NOTE: These need to happen in separate transactions.
  (cf/ensure-conforms
   conn
   {:seed/catalogues {:txes [(catalogues conn)]}})
  (cf/ensure-conforms
   conn
   {:seed/approval {:txes [(approval-tx conn)]}})
  (cf/ensure-conforms
   conn
   {:seed/member-licenses {:txes [(member-licenses-tx conn)]}})
  (cf/ensure-conforms
   conn
   {:seed/rent-payments {:txes [(rent-payments-tx conn)]}}) )


(comment
  (services-tx)

  )
