(ns starcity.datomic.migrations.add-has-pet-attr-10-3-16
  (:require [starcity.models.util :refer :all]
            [datomic.api :as d]))

(def add-has-pet-attr
  {:schema/add-has-pet-attr-10-3-16
   {:txes     [[{:db/id                 #db/id[:db.part/db]
                 :db/ident              :member-application/has-pet
                 :db/valueType          :db.type/boolean
                 :db/cardinality        :db.cardinality/one
                 :db/doc                "Whether or not applicant has a pet."
                 :db.install/_attribute :db.part/db}]]}})

(def seed-has-pet
  {:seed/seed-has-pet-10-3-16
   {:txes     [(fn [conn]
                 (let [with-pet    (qes '[:find ?ma
                                          :where
                                          [?ma :member-application/pet _]]
                                        (d/db conn))
                       without-pet (qes '[:find ?ma
                                          :where
                                          [_ :account/member-application ?ma]
                                          [(missing? $ ?ma :member-application/pet)]]
                                        (d/db conn))]
                   (vec
                    (concat
                     (map (fn [{e :db/id}] [:db/add e :member-application/has-pet true]) with-pet)
                     (map (fn [{e :db/id}] [:db/add e :member-application/has-pet false]) without-pet)))))]
    :requires [:schema/add-has-pet-attr-10-3-16
               :starcity/add-member-application-schema]}})
