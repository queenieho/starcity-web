(ns starcity.models.application
  (:require [starcity.datomic.util :refer :all]
            [starcity.models.util :refer :all]
            [starcity.datomic :refer [conn]]
            [starcity.config :refer [datomic-partition]]
            [datomic.api :as d]
            [plumbing.core :refer [assoc-when defnk]]
            [clojure.spec :as s]
            [clojure.string :refer [trim capitalize]]))

;; =============================================================================
;; Helper Specs
;; =============================================================================

;; =====================================
;; Pets

(s/def ::type #{:dog :cat})
(s/def ::breed string?)
(s/def ::weight pos-int?)

(defmulti pet-type :type)
(defmethod pet-type :dog [_] (s/keys :req-un [::type ::breed ::weight]))
(defmethod pet-type :cat [_] (s/keys :req-un [::type]))

(s/def ::pet (s/multi-spec pet-type :type))
(s/def ::pets (s/+ ::pet))

;; =============================================================================
;; API
;; =============================================================================

(defn create!
  "Create a new rental application for `account-id'."
  [account-id desired-lease desired-availability & {:keys [pets]}]
  (let [tid (d/tempid (datomic-partition))
        ent (-> {:db/id                tid
                 :desired-lease        desired-lease
                 :desired-availability desired-availability}
                (assoc-when :pets (map (partial ks->nsks :pet) pets))
                (assoc :account/_application account-id))
        tx  @(d/transact conn [(ks->nsks :rental-application ent)])]
    (d/resolve-tempid (d/db conn) (:tempids tx) tid)))

(s/def ::new-application (s/cat :account-id int?
                                :desired-lease int?
                                :desired-availability (s/spec (s/+ :starcity.spec/date))
                                :opts (s/keys* :opt-un [::pets])))
(s/fdef create!
        :args ::new-application
        :ret  int?)

(comment

  (let [acct (one (d/db conn) :account/email "test@test.com") ]
    (create! (:db/id acct) 75919105938 [#inst "2016-09-01T00:00:00.000-00:00"]
             :pets [{:type :cat}]))

  (let [acct (one (d/db conn) :account/email "test@test.com")]
    (d/touch (:account/application acct)))

  )
