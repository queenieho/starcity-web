(ns starcity.datomic.seed.applications
  (:require [starcity.datomic.partition :refer [tempid]]
            [plumbing.core :refer [assoc-when]]
            [datomic.api :as d]
            [clj-time.core :as t]
            [clj-time.coerce :as c]))

(defn license [conn term]
  (d/q '[:find ?e .
         :in $ ?term
         :where [?e :license/term ?term]]
       (d/db conn) term))

(defn application
  [account-id & {:keys [address properties license move-in pet
                        community-fitness status]
                 :or   {move-in (c/to-date (t/plus (t/now) (t/weeks 2)))
                        status  :member-application.status/in-progress}}]
  (let [id (tempid)]
    [{:db/id                      account-id
      :account/member-application id}

     (assoc-when {:db/id                     id
                  :member-application/status status}
                 :member-application/desired-license license
                 :member-application/desired-properties properties
                 :member-application/current-address address
                 :member-application/desired-availability move-in
                 :member-application/has-pet (boolean pet)
                 :member-application/community-fitness community-fitness)]))

(defn seed [conn]
  @(d/transact conn (concat
                     (application [:account/email "test@test.com"]
                                  :license (license conn 3))
                     (application [:account/email "onboarding@test.com"]
                                  :license (license conn 6)))))
