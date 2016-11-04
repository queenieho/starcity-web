(ns starcity.api.admin.accounts.entry
  (:require [datomic.api :as d]
            [starcity.datomic :refer [conn]]
            [starcity.models.util :refer :all]
            [clojure.spec :as s]
            [starcity.models.account :as account]
            [starcity.api.common :as api]))

(defn- role [account]
  (let [r (:account/role account)]
    (case r
      :account.role/tenant "member"
      (name r))))

(defn- parse [account]
  {:id           (:db/id account)
   :full-name    (account/full-name account)
   :phone-number (:account/phone-number account)
   :email        (:account/email account)
   :role         (role account)})

(defn fetch
  [account-id]
  (-> (d/entity (d/db conn) account-id)
      (parse)
      (api/ok)))
