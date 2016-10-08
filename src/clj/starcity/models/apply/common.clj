(ns starcity.models.apply.common
  (:require [starcity.datomic :refer [conn]]
            [starcity.models.util :refer [qe1]]
            [starcity.spec]
            [datomic.api :as d]
            [clojure.spec :as s]))

(defn account-id
  "Given an `application` entity, produce the `account-id`."
  [application]
  (:db/id (first (:account/_member-application application))))

(s/fdef account-id
        :args (s/cat :application :starcity.spec/entity)
        :ret integer?)

(defn by-account-id
  "Retrieve an application by account id."
  [account-id]
  (qe1
   '[:find ?e
     :in $ ?acct
     :where
     [?acct :account/member-application ?e]]
   (d/db conn) account-id))

(s/fdef by-account-id
        :args (s/cat :account-id :starcity.spec/lookup)
        :ret :starcity.spec/entity)
