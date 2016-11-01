(ns starcity.api.admin.accounts.list
  (:require [datomic.api :as d]
            [starcity.datomic :refer [conn]]
            [starcity.models.util :refer :all]
            [clojure.spec :as s]
            [starcity.models.account :as account]
            [starcity.api.common :as api]))

(def ^:private view->role
  {:members    :account.role/tenant
   :applicants :account.role/applicant
   :pending    :account.role/pending})

(def keyfn
  {:name #(str (:account/first-name %) (:account/last-name %))})

(defn- order [direction coll]
  (if (#{:asc} direction)
    (reverse coll)
    coll))

(defn- parse [offset index account]
  {:id           (:db/id account)
   :number       (+ index offset)
   :name         (account/full-name account)
   :email        (:account/email account)
   :phone-number (:account/phone-number account)})

(defmulti accounts-query
  "Query the accounts matching the provided `view`."
  (fn [view] view))

(defmethod accounts-query :all [_]
  (qes '[:find ?e
         :where
         [?e :account/email _]
         (not [?e :account/role :account.role/admin])]
       (d/db conn)))

(defmethod accounts-query :default [view]
  (qes '[:find ?e
         :in $ ?role
         :where
         [?e :account/email _]
         [?e :account/role ?role]]
       (d/db conn) (view->role view)))

(defn- accounts [limit offset direction sort-key view]
  (->> (accounts-query view)
       (sort-by (get keyfn sort-key))
       (order direction)
       (drop offset)
       (take limit)
       (map-indexed (partial parse offset))))

(defmulti total
  "Determine the total number of accounts for this view."
  (fn [view] view))

(defmethod total :all [view]
  (ffirst
   (d/q '[:find (count ?e)
          :where
          [?e :account/email _]
          (not [?e :account/role :account.role/admin])]
        (d/db conn))))

(defmethod total :default [view]
  (ffirst
   (d/q '[:find (count ?e)
          :in $ ?role
          :where
          [?e :account/email _]
          [?e :account/role ?role]]
        (d/db conn) (view->role view))))

(defn fetch
  [limit offset direction sort-key view]
  (api/ok
   {:accounts (accounts limit offset direction sort-key view)
    :total    (total view)}))

(s/fdef fetch
        :args (s/cat :limit integer?
                     :offset (s/and integer? #(>= % 0))
                     :direction #{:asc :desc}
                     :sort-key #{:name}
                     :view #{:all :members :applicants :pending}))

(comment

  (d/q '[:find ?ent ?inst
         :in $ ?ent ?attr ?val
         :where
         [?ent ?attr ?val ?t true]
         [?t :db/txInstant ?inst]]
       (d/db conn) [:account/email "test@test.com"] :account/email "test@test.com")

  (d/q '[:find ?email ?inst
         ;; :in $ ?ent ?attr ?val
         :where
         [?e :account/activation-hash _ ?t true]
         [?e :account/email ?email]
         [?t :db/txInstant ?inst]]
       (d/db conn))



  )
