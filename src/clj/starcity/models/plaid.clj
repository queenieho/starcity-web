(ns starcity.models.plaid
  (:require [starcity.datomic.util :refer :all]
            [starcity.datomic :refer [conn]]
            [starcity.models.util :refer :all]
            [starcity.config :refer [config]]
            [datomic.api :as d]
            [plumbing.core :refer [assoc-when]]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- extract-income-stream
  [{:keys [active confidence days monthly_income period]}]
  (ks->nsks :income-stream
            {:active     active
             :confidence (float confidence)
             :days       days
             :income     monthly_income
             :period     period}))

(defn- extract-income
  [{:keys [income_streams last_year_income last_year_income_before_tax
           projected_yearly_income projected_yearly_income_before_tax]}]
  (ks->nsks :plaid-income
            {:last-year                last_year_income
             :last-year-pre-tax        last_year_income_before_tax
             :projected-yearly         projected_yearly_income
             :projected-yearly-pre-tax projected_yearly_income_before_tax
             :obtained-at              (java.util.Date.)
             :income-streams           (map extract-income-stream income_streams)}))

(defn- extract-account
  [{:keys [balance institution_type subtype type]}]
  (ks->nsks :bank-account
            (assoc-when
             {:available-balance (float (:available balance))
              :current-balance   (float (:current balance))
              :type              type
              :institution-type  institution_type
              :obtained-at       (java.util.Date.)}
             :subtype subtype)))

(defn- add-income-tx
  [entity-id access-token {:keys [accounts income]}]
  {:db/id               entity-id
   :plaid/income        (extract-income income)
   :plaid/bank-accounts (map extract-account accounts)
   :plaid/access-token  access-token})

;; =============================================================================
;; API
;; =============================================================================

(defn by-account-id
  [account-id]
  (one (d/db conn) :plaid/account account-id))

(defn create!
  [account-id public-token access-token]
  (let [ent (ks->nsks :plaid {:account                  account-id
                              :public-token             public-token
                              :access-token             access-token
                              :access-token-obtained-at (java.util.Date.)})
        tid (d/tempid (get-in config [:datomic :partition]))
        tx  @(d/transact conn [(assoc ent :db/id tid)])]
    (d/resolve-tempid (d/db conn) (:tempids tx) tid)))

(defn add-income-data!
  [access-token data]
  (let [ent (one (d/db conn) :plaid/access-token access-token)]
    (d/transact conn [(add-income-tx (:db/id ent) access-token data)])))

(comment

  (d/touch
   (by-account-id (:db/id (one (d/db conn) :account/email "test@test.com"))))

  ;; add income data
  (let [account-id (:db/id (one (d/db conn) :account/email "test@test.com"))]
    (add-income-data! account-id "abc" sample-income-data)
    )

  ;; inspect
  (let [account-id (:db/id (one (d/db conn) :account/email "test@test.com"))]
    (d/touch (lookup account-id))
    )

  (def sample-income-data
    {:accounts
     {},
     :income
     {},
     :access_token
     ""})


  (let [account-id (:db/id (one (d/db conn) :account/email "test@test.com"))]
    (create! account-id "abc" "def"))


  )
