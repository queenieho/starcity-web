(ns starcity.api.admin.accounts
  (:require [bouncer
             [core :as b]
             [validators :as v]]
            [clj-time
             [coerce :as c]
             [core :as t]
             [format :as f]]
            [clojure
             [spec :as s]
             [string :as string]]
            [compojure.core :refer [defroutes GET POST]]
            [datomic.api :as d]
            [plumbing.core :as plumbing]
            [starcity
             [auth :as auth]
             [datomic :refer [conn]]]
            [starcity.models
             [account :as account]
             [application :as app]
             [approval :as approval]
             [charge :as charge]
             [income-file :as income-file]
             [license :as license]
             [member-license :as member-license]
             [msg :as msg]
             [note :as note]
             [property :as property]
             [rent-payment :as rent-payment]
             [security-deposit :as deposit]
             [unit :as unit]]
            [starcity.util
             [response :as response]
             [validation :as uv]]
            [toolbelt
             [core :as tb :refer [str->int]]
             [datomic :as td]
             [predicates :as p]]
            [toolbelt.date :as date]))

;; =============================================================================
;; Common
;; =============================================================================

(def transit "application/transit+json")

(def stripe-dashboard-uri
  "https://dashboard.stripe.com")

;; =====================================
;; Clientize Security Deposit

(defn- check->payment [check]
  (merge
   {:payment/id     (:db/id check)
    :payment/method :check
    :payment/status (name (:check/status check))
    :payment/amount (:check/amount check)}
   (select-keys check [:check/name :check/bank :check/number
                       :check/received-on :check/date])))

(defn- charge->payment [charge]
  {:payment/id        (:db/id charge)
   :payment/method    :ach
   :payment/status    (name (:charge/status charge))
   :payment/amount    (:charge/amount charge)
   :charge/stripe-uri (format "%s/payments/%s"
                              stripe-dashboard-uri (:charge/stripe-id charge))})

(defn- clientize-security-deposit [deposit]
  (let [checks  (deposit/checks deposit)
        charges (deposit/charges deposit)]
    {:db/id            (:db/id deposit)
     :deposit/received (deposit/received deposit)
     :deposit/required (deposit/required deposit)
     :deposit/pending  (deposit/amount-pending deposit)
     :deposit/due-date (deposit/due-by deposit)
     :deposit/method   (deposit/method deposit)
     :deposit/payments (concat
                        (map check->payment checks)
                        (map charge->payment charges))}))

(s/fdef clientize-security-deposit
        :args (s/cat :deposit p/entity?)
        :ret map?)

;; =============================================================================
;; Accounts Search
;; =============================================================================

(def search-rules
  '[[(search ?account ?query) [(fulltext $ :account/first-name ?query) [[?account]]]]
    [(search ?account ?query) [(fulltext $ :account/middle-name ?query) [[?account]]]]
    [(search ?account ?query) [(fulltext $ :account/last-name ?query) [[?account]]]]
    [(search ?account ?query) [(fulltext $ :account/email ?query) [[?account]]]]])

(defn- search-accounts* [db q]
  (letfn [(-clientize [a]
            {:db/id         (:db/id a)
             :account/name  (account/full-name a)
             :account/email (:account/email a)})]
    (->> (d/q '[:find [?e ...]
                :in $ ?q %
                :where
                (search ?e ?q)]
              db (str q "*") search-rules)
         (map (comp -clientize (partial d/entity db))))))

(defn- search-accounts
  "Searches our database of accounts by query term `q`."
  [conn q]
  {:result (if (empty? q) [] (search-accounts* (d/db conn) q))})

;; =============================================================================
;; Fetch Account
;; =============================================================================

(defn- contact [account]
  {:account/email (:account/email account)
   :account/phone (:account/phone-number account)
   :account/dob   (when-let [dob (:account/dob account)]
                    (f/unparse (f/formatter "M/d") (c/to-date-time dob)))})

(defmulti clientize-account
  "Produce a map of `client-data` based on the role of the account. This is
  because different information is relevant to the admin dashboard based on the
  role that `account` possesses."
  (fn [conn account client-data]
    (:account/role account)))

(defn- fetch-account
  "Fetch an account by `account-id`."
  [conn account-id]
  (let [account (d/entity (d/db conn) account-id)]
    {:result
     (clientize-account conn account {:db/id           account-id
                                      :account/name    (account/full-name account)
                                      :account/role    (:account/role account)
                                      :account/contact (contact account)})}))

;; =============================================================================
;; Applicant

(defn- income [conn account]
  {:income/files
   (map (fn [file]
          (-> (select-keys file [:db/id :income-file/path])
              (assoc :income-file/name (last (string/split (:income-file/path file) #"/")))
              (dissoc :income-file/path)))
        (income-file/by-account conn account))})

(defn- address
  [{:keys [address/locality address/region address/postal-code address/country]}]
  (when (and locality region postal-code country)
    (format "%s %s, %s, %s" locality region postal-code (or country "US"))))

(defn- updated-at
  "We need to examine three different entities to determine when the application
  was last updated, since part of the application updates fields on these
  entities. This function examines the time that the `:member-application`,
  `:account` and `:community-fitness` entities were last updated and picks the
  latest update."
  [conn account app]
  (->> [app account (app/community-fitness app)]
       (remove nil?)                    ; in case community fitness is not present
       (map (comp c/to-date-time (partial td/updated-at (d/db conn))))
       (t/latest)
       (c/to-date)))

(defn application [conn account]
  (when-let [app (app/by-account conn account)]
    (merge
     {:application/status      (app/status app)
      ;; NOTE: Think about how to best handle the timezone. This is hardcoded
      ;; for now since we're operating exclusively on the west coast. This
      ;; should probably come from the preferred time zone of the
      ;; /viewer/ (admin)
      :application/move-in     (date/to-utc-corrected-date
                                (app/move-in-date app)
                                (t/time-zone-for-id "America/Los_Angeles"))
      :application/license     (select-keys (app/desired-license app)
                                            [:db/id :license/term])
      :application/has-pet     (app/has-pet? app)
      :application/pet         (into {} (app/pet app))
      :application/communities (map #(select-keys % [:property/name :db/id])
                                    (app/communities app))
      :application/address     (address (app/address app))
      :application/fitness     (into {} (app/community-fitness app))
      :application/updated-at  (updated-at conn account app)
      :application/created-at  (td/created-at (d/db conn) app)}
     (income conn account))))

(defmethod clientize-account :account.role/applicant
  [conn account client-data]
  (plumbing/assoc-when client-data :account/application (application conn account)))

;; =============================================================================
;; Member

(defn- payment-methods [conn account]
  (when-let [license (member-license/active conn account)]
    {:payment/autopay (member-license/autopay-on? license)
     :payment/bank    (account/bank-linked? account)}))

(defn- security-deposit [_ account]
  (clientize-security-deposit (account/security-deposit account)))

(defn- query-payments
  "Retrieve ALL rent payments for `account`, regardless of license."
  [conn account]
  (->> (d/q '[:find [?p ...]
              :in $ ?a
              :where
              [?a :account/license ?m]
              [?m :member-license/rent-payments ?p]]
            (d/db conn) (:db/id account))
       (map (partial d/entity (d/db conn)))
       (sort-by :rent-payment/period-start)
       (reverse)))

(defn- clientize-check [check]
  (select-keys check [:db/id
                      :check/name
                      :check/bank
                      :check/number
                      :check/amount
                      :check/status
                      :check/received-on
                      :check/date]))

(defn- payment-uri [payment]
  (let [method     (rent-payment/method payment)
        charge-id  (-> payment rent-payment/charge charge/id)
        invoice-id (rent-payment/invoice payment)
        managed-id (-> payment rent-payment/member-license member-license/managed-account-id)]
    (case method
      :rent-payment.method/ach
      (format "%s/payments/%s" stripe-dashboard-uri charge-id)

      :rent-payment.method/autopay
      (format "%s/%s/invoices/%s" stripe-dashboard-uri managed-id invoice-id)

      nil)))

(defn- clientize-payment [payment]
  (-> (select-keys payment [:db/id
                            :rent-payment/status
                            :rent-payment/period-start
                            :rent-payment/period-end
                            :rent-payment/due-date
                            :rent-payment/paid-on
                            :rent-payment/amount
                            :rent-payment/method-desc
                            :rent-payment/method
                            :rent-payment/check])
      (update :rent-payment/check clientize-check)
      (assoc :rent-payment/stripe-uri (payment-uri payment))))

(defn- payments [conn account]
  (->> (query-payments conn account)
       (map clientize-payment)))

(defn- clientize-member-license [license]
  (let [unit (:member-license/unit license)]
    {:license/status   (:member-license/status license)
     :license/term     (get-in license [:member-license/license :license/term])
     :license/starts   (:member-license/commencement license)
     :license/ends     (:member-license/ends license)
     :license/rate     (:member-license/rate license)
     :license/unit     (select-keys unit [:db/id :unit/name])
     :license/property (select-keys (unit/property unit)
                                    [:db/id :property/name])}))

(defn- member-licenses [conn account]
  (->> (:account/licenses account)
       (map clientize-member-license)
       (sort-by :license/active)
       (reverse)))

(defmethod clientize-account :account.role/member
  [conn account client-data]
  (-> (zipmap [:account/payment
               :account/deposit
               :account/rent-payments
               :account/member-licenses
               :account/application]
              ((juxt payment-methods
                     security-deposit
                     payments
                     member-licenses
                     application)
               conn account))
      (merge client-data)))

;; =============================================================================
;; Onboarding

(defmethod clientize-account :account.role/onboarding
  [conn account client-data]
  (let [approval (account/approval account)
        approver (approval/approver approval)
        deposit  (account/security-deposit account)]
    (merge
     client-data
     {:account/approval
      {:approval/move-in  (approval/move-in approval)
       :approval/approver (account/full-name approver)
       :approval/unit     (select-keys (approval/unit approval) [:db/id :unit/name])
       :approval/term     (-> approval approval/license license/term)}

      :account/deposit     (clientize-security-deposit deposit)
      :account/application (application conn account)})))

;; =====================================
;; Catch-all

(defmethod clientize-account :default [_ _ client-data] client-data)

;; =============================================================================
;; Accounts Overview
;; =============================================================================

(defn- query-members
  "Query for all accounts with active licenses that have a unit assigned."
  [conn]
  (d/q '[:find
         (pull ?p [:db/id :property/name])
         (pull ?a [:db/id :account/first-name :account/last-name])
         (pull ?u [:db/id :unit/name])
         (pull ?status [:db/ident])
         :in $ ?now
         :where
         [?a :account/licenses ?l]
         [?l :member-license/status :member-license.status/active]
         [?p :property/units ?u]
         [?l :member-license/unit ?u]
         [?l :member-license/rent-payments ?py]
         [?py :rent-payment/period-start ?start]
         [?py :rent-payment/period-end ?end]
         [?py :rent-payment/status ?status]
         [(.after ^java.util.Date ?end ?now)]
         [(.before ^java.util.Date ?start ?now)]]
       (d/db conn) (java.util.Date.)))

(defn- member [[property account unit rent-status]]
  {:db/id               (:db/id account)
   :account/name        (str (:account/first-name account) " " (:account/last-name account))
   :account/property    property
   :account/unit        unit
   :account/rent-status (:db/ident rent-status)})

(defn members [conn]
  (->> (query-members conn)
       (map member)
       (sort-by (comp :unit/name :account/unit))))

(defn- full-name [m]
  (assoc m :account/name (str (:account/first-name m) " " (:account/last-name m))))

(defn recently-created-applicants
  "Query for the most recent applicants within the last thirty days."
  [conn]
  (letfn [(-clientize [[a created-at]]
            (assoc a :account/created-at created-at))]
    (->> (d/q '[:find (pull ?a [:db/id :account/first-name
                                :account/last-name :account/email]) ?tx-time
                :in $ ?after
                :where
                [?a :account/role :account.role/applicant ?tx _]
                [?tx :db/txInstant ?tx-time]
                [(.before ^java.util.Date ?after ?tx-time)]]
              (d/db conn) (c/to-date (t/minus (t/now) (t/days 30))))
         (sort-by second)
         (map (comp full-name -clientize))
         (reverse))))

(defn recently-active-applicants
  "Query for the most recently active applicants in the past sixty days.
  I'm currently defining 'active' as having some modification made to the
  `:account/application` entity or `:application/fitness` entity. This misses
  some fields, but should give a fairly good idea."
  [conn]
  (letfn [(-clientize [[id & dts]]
            (-> (d/entity (d/db conn) id)
                (select-keys [:db/id :account/first-name :account/last-name :account/email])
                (assoc :account/updated-at (t/latest dts))
                (full-name)))]
    (->> (d/q '[:find ?a (max ?tx-time-app) (max ?tx-time-fitness)
                :in $ ?after
                :where
                [?a :account/role :account.role/applicant]
                [?a :account/application ?app]
                [?app _ _ ?tx-app _]
                [?app :application/fitness ?f]
                [?f _ _ ?tx-fitness _]
                [?tx-fitness :db/txInstant ?tx-time-fitness]
                [?tx-app :db/txInstant ?tx-time-app]
                [(.before ^java.util.Date ?after ?tx-time-app)]]
              (d/history (d/db conn)) (c/to-date (t/minus (t/now) (t/days 60))))
         (sort-by (comp t/latest rest))
         (map -clientize)
         ;; NOTE: This (`distinct-by`) is used to remove records that may have had
         ;; administrative bulk modifications done (the assumption is that it is
         ;; highly unlikely that any two modifications by members will happen at
         ;; the exact same instant in time). Can be removed after we
         ;; implement proper *provenance* on entity txes -- then our queries can
         ;; incorporate not just /when/ something was modified, but /by whom/.
         (plumbing/distinct-by :account/updated-at)
         (reverse))))

(defn recently-submitted-applicants
  "Applications that have been submitted in the last thirty days that still have
  status `:application.status/submitted`."
  [conn]
  (->> (d/q '[:find (pull ?a [:db/id :account/first-name
                              :account/last-name :account/email]) ?tx-time
              :in $ ?after
              :where
              [?a :account/application ?app]
              [?app :application/status :application.status/submitted ?tx _]
              [?tx :db/txInstant ?tx-time]
              [(.after ^java.util.Date ?after ?tx-time)]]
            (d/db conn) (java.util.Date.) #_(c/to-date (t/minus (t/now) (t/days 30))))
       (sort-by second)
       (map (fn [[a submitted-at]]
              (-> (assoc a :application/submitted-at submitted-at)
                  (full-name))))
       (reverse)))

(defn accounts-overview
  "Assemble views of account-related data."
  [conn]
  {:result {:accounts/members     (members conn)
            :applicants/created   (recently-created-applicants conn)
            :applicants/active    (recently-active-applicants conn)
            :applicants/submitted (recently-submitted-applicants conn)}})

;; =============================================================================
;; Approve
;; =============================================================================


(def ^:private cannot-approve-error
  "This application cannot be approved! This could be because the application belongs to a non-applicant, is not yet complete, or is already approved.")


(def ^:private unit-occupied-error
  "The provided unit is not available.")


(defn approve-account!
  [conn approver account-id unit-id license-id move-in]
  (let [account  (d/entity (d/db conn) account-id)
        unit     (d/entity (d/db conn) unit-id)
        license  (d/entity (d/db conn) license-id)
        resp-err #(response/transit-unprocessable {:error %})]
    (cond

      (some nil? [account unit license])
      (response/transit-malformed {:message "Invalid account, unit or license."})

      (not (unit/available? (d/db conn) unit move-in))
      (resp-err unit-occupied-error)

      (not (account/can-approve? account))
      (resp-err cannot-approve-error)

      :otherwise
      (do
        @(d/transact conn (approval/approve approver account unit license move-in))
        (response/transit-ok {:result "ok"})))))


;; =============================================================================
;; Notes
;; =============================================================================

;; =============================================================================
;; Fetch Notes

(defn- clientize-note [db [note-id created-at]]
  (let [note (d/entity db note-id)]
    (note/clientize db note created-at)))

(defn- query-notes [db account-id]
  (->> (d/q '[:find ?note ?tx-time
              :in $ ?account
              :where
              [?account :account/notes ?note ?tx]
              [?tx :db/txInstant ?tx-time]]
            db account-id)
       (sort-by second)
       (reverse)
       ;; TODO: Pagination
       (map (partial clientize-note db))))

(defn fetch-notes
  "Fetch all notes that are attached to the account identified by `account-id`."
  [conn account-id]
  {:result (query-notes (d/db conn) account-id)})

;; =============================================================================
;; Create Note

(defn create-note!
  "Create a new note under `account-id` given `params`."
  [conn account-id author {:keys [subject content notify ticket]}]
  (let [note (note/create subject content :ticket? ticket :author author)]
    @(d/transact conn [{:db/id         account-id
                        :account/notes note}
                       (msg/note-created note notify)])
    {:result "ok"}))

(s/def ::subject string?)
(s/def ::content string?)
(s/def ::notify boolean?)
(s/def ::ticket boolean?)
(s/fdef create-note!
        :args (s/cat :conn p/conn?
                     :account-id integer?
                     :author p/entity?
                     :params (s/keys :req-un [::subject ::content]
                                     :opt-un [::notify ::ticket]))
        :ret (s/keys :req-un [::result]))

;; =============================================================================
;; Routes
;; =============================================================================

(defroutes routes

  (GET "/overview" []
       (fn [_]
         (response/transit-ok (accounts-overview conn))))

  (GET "/autocomplete" [q]
       (fn [_]
         (response/transit-ok (search-accounts conn q))))

  (GET "/:account-id" [account-id]
       (fn [_]
         (response/transit-ok (fetch-account conn (str->int account-id)))))

  (POST "/:account-id/approve" [account-id]
        (fn [{:keys [params] :as req}]
          (let [approver (auth/requester req)]
            (approve-account! conn approver
                              (str->int account-id)
                              (:unit-id params)
                              (:license-id params)
                              (:move-in params)))))

  ;; =====================================
  ;; Notes

  (GET "/:account-id/notes" [account-id]
       (fn [_]
         (response/transit-ok
          (fetch-notes conn (str->int account-id)))))

  (POST "/:account-id/notes" [account-id]
        (fn [{params :params :as req}]
          (let [vresult (b/validate params {:subject [v/required v/string]
                                            :content [v/required v/string]
                                            :notify  v/boolean
                                            :ticket  v/boolean})
                author  (auth/requester req)]
            (if-let [params (uv/valid? vresult)]
              (response/transit-ok (create-note! conn (str->int account-id) author params))
              (response/transit-malformed {:message (first (uv/errors vresult))}))))))
