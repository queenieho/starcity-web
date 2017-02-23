(ns starcity.models.income-file
  (:require [datomic.api :as d]
            [starcity.datomic :refer [conn]]
            [starcity.models.util :refer [qes]]))

(defn create
  "Produce transaction data to create a new income file."
  [account content-type path size]
  {:income-file/account      (:db/id account)
   :income-file/content-type content-type
   :income-file/path         path
   :income-file/size         (long size)})

(defn by-account
  "Fetch the income files for this account."
  [conn account]
  (qes '[:find ?e
         :in $ ?a
         :where
         [?e :income-file/account ?a]]
       (d/db conn) (:db/id account)))
