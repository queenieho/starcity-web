(ns starcity.api.admin.accounts.list
  (:require [clojure.spec :as s]
            [datomic.api :as d]
            [starcity.api.admin.common :refer [order-by]]
            [starcity.api.common :as api]
            [starcity.datomic :refer [conn]]
            [starcity.models
             [account :as account]
             [util :refer :all]]
            [clj-time.coerce :as c]))

;; =============================================================================
;; Internal
;; =============================================================================

(def ^:private view->role
  {:members    :account.role/member
   :applicants :account.role/applicant
   :pending    :account.role/onboarding})

(def ^:private key->sortfn
  {:name       #(str (:account/first-name %) (:account/last-name %))
   :created-at (comp c/to-long account/created-at)})

(defn- parse [offset index account]
  {:id           (:db/id account)
   :number       (+ index offset)
   :name         (account/full-name account)
   :email        (:account/email account)
   :phone-number (:account/phone-number account)
   :created-at   (account/created-at account)})

(def ^:private search-rules
  '[[(search ?account ?query) [(fulltext $ :account/first-name ?query) [[?account]]]]
    [(search ?account ?query) [(fulltext $ :account/middle-name ?query) [[?account]]]]
    [(search ?account ?query) [(fulltext $ :account/last-name ?query) [[?account]]]]
    [(search ?account ?query) [(fulltext $ :account/email ?query) [[?account]]]]])

;; TODO: This is terrible
(defn- query* [q & clauses]
  (let [query '[:find ?acct
                :in $ ?q %
                :where
                [?acct :account/email _]
                (not [?acct :account/role :account.role/admin])]
        query (if (empty? q) query (conj query '(search ?acct ?q)))]
    (qes (reduce #(conj %1 %2) query clauses)
         (d/db conn) (str q "*") search-rules)))

(defmulti ^:private query-accounts
  "Query the accounts matching the provided `view`."
  (fn [view _] view))

(defmethod query-accounts :all [_ q]
  (query* q))

(defmethod query-accounts :default [view q]
  (query* q ['?acct :account/role (view->role view)]))

(defn- accounts [limit offset direction sort-key view q]
  (->> (query-accounts view q)
       (sort-by (get key->sortfn sort-key))
       (order-by direction)
       (drop offset)
       (take limit)
       (map-indexed (partial parse offset))))

;; =============================================================================
;; Total

(defn- total* [q & clauses]
  (let [query '[:find (count ?acct)
                :in $ ?q %
                :where
                [?acct :account/email _]
                (not [?acct :account/role :account.role/admin])]
        query (if (empty? q) query (conj query '(search ?acct ?q)))]
    (ffirst (d/q (reduce #(conj %1 %2) query clauses)
                 (d/db conn) (str q "*") search-rules))))

(defmulti ^:private total
  "Determine the total number of accounts for this view."
  (fn [view _] view))

(defmethod total :all [_ q]
  (total* q))

(defmethod total :default [view q]
  (total* q ['?acct :account/role (view->role view)]))

;; =============================================================================
;; API
;; =============================================================================

;; TODO: (s/+ ::account)
(s/def ::accounts sequential?)
(s/def ::total integer?)

(defn fetch
  "Fetch `limit` accounts, offset by `offset`, sorted by `sort-key` in either
  ascending or descending order (`direction`), under a specific `view`."
  [limit offset direction sort-key view query]
  (api/ok
   {:accounts (accounts limit offset direction sort-key view query)
    :total    (total view query)}))

(s/fdef fetch
        :args (s/cat :limit integer?
                     :offset (s/and integer? #(>= % 0))
                     :direction #{:asc :desc}
                     :sort-key #{:name :created-at}
                     :view #{:all :members :applicants :pending}
                     :query string?))
