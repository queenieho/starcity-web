(ns starcity.api.admin.applications.list
  (:require [clojure.spec :as s]
            [datomic.api :as d]
            [starcity.api.admin.common :refer [order-by]]
            [starcity.datomic :refer [conn]]
            [starcity.models
             [account :as account]
             [application :as application]
             [util :refer :all]]
            [starcity.api.common :as api]
            [clj-time.coerce :as c]))

;; =============================================================================
;; Internal
;; =============================================================================

;; =============================================================================
;; Applications

(def ^:private keyfns
  {:name         application/full-name
   :term         application/desired-term
   :move-in      (comp c/to-long :member-application/desired-availability)
   :completed-at (comp c/to-long :member-application/submitted-at)})

(defn- parse [offset index application]
  (let [account (first (:account/_member-application application))]
    {:id           (:db/id application)
     :number       (+ index offset)
     :name         (application/full-name application)
     :email        (:account/email account)
     :phone-number (:account/phone-number account)
     :properties   (->> (:member-application/desired-properties application)
                        (map :property/name))
     :term         (application/desired-term application)
     :move-in      (:member-application/desired-availability application)
     :completed    (application/completed? application)
     :completed-at (:member-application/submitted-at application)
     :approved     (application/approved? application)}))

(def ^:private search-rules
  '[[(search ?account ?query) [(fulltext $ :account/first-name ?query) [[?account]]]]
    [(search ?account ?query) [(fulltext $ :account/middle-name ?query) [[?account]]]]
    [(search ?account ?query) [(fulltext $ :account/last-name ?query) [[?account]]]]
    [(search ?account ?query) [(fulltext $ :account/email ?query) [[?account]]]]
    [(search ?account ?query)
     [?account :account/member-application ?app]
     [?app :member-application/desired-properties ?p]
     [(fulltext $ :property/name ?query) [[?p]]]]])

;; TODO: This is terrible
(defn- query* [q & clauses]
  (let [query '[:find ?app
                :in $ ?q %
                :where
                [?acct :account/member-application ?app]
                (not [?acct :account/role :account.role/admin])]
        query (if (empty? q) query (conj query '(search ?acct ?q)))]
    (qes (reduce #(conj %1 %2) query clauses)
         (d/db conn) (str q "*") search-rules)))

(defmulti ^:private query-applications (fn [view _] view))

(defmethod query-applications :all [_ q]
  (query* q))

(defmethod query-applications :complete [_ q]
  (query* q '[?app :member-application/locked true]))

(defmethod query-applications :incomplete [_ q]
  (query* q '[?app :member-application/locked false]))

(defn- applications [limit offset direction sort-key view query]
  (->> (query-applications view query)
       (sort-by (get keyfns sort-key))
       (order-by direction)
       (drop offset)
       (take limit)
       (map-indexed (partial parse offset))))

;; =============================================================================
;; Total

(defn- total* [q & clauses]
  (let [query '[:find (count ?app)
                :in $ ?q %
                :where
                [?acct :account/member-application ?app]
                (not [?acct :account/role :account.role/admin])]
        query (if (empty? q) query (conj query '(search ?acct ?q)))]
    (ffirst (d/q (reduce #(conj %1 %2) query clauses)
                 (d/db conn) (str q "*") search-rules))))

(defmulti ^:private total
  "Determine the total number of applications for this view."
  (fn [view _] view))

(defmethod total :all [_ q] (total* q))

(defmethod total :complete [_ q]
  (total* q '[?app :member-application/locked true]))

(defmethod total :incomplete [_ q]
  (total* q '[?app :member-application/locked false]))

;; =============================================================================
;; API
;; =============================================================================

(s/def ::applications sequential?)
(s/def ::total integer?)
(s/def ::sort-key #{:name :term :move-in :completed-at})
(s/def ::view #{:all :complete :incomplete})

(defn fetch
  "Fetch `limit` accounts, offset by `offset` and sorted by `sort-key` in either
  ascending or descending order (`direction`)."
  [limit offset direction sort-key view query]
  (api/ok
   {:applications (applications limit offset direction sort-key view query)
    :total        (or (total view query) 0)}))

(s/fdef fetch
        :args (s/cat :limit integer?
                     :offset (s/and integer? #(>= % 0))
                     :direction :starcity.api.admin.common/direction
                     :sort-key ::sort-key
                     :view ::view
                     :query string?))
