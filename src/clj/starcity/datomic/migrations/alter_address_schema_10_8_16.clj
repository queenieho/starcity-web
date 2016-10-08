(ns starcity.datomic.migrations.alter-address-schema-10-8-16
  "This alteration adds support for international addresses."
  (:require [starcity.models.util :refer [find-all-by]]
            [datomic.api :as d]))

(def alter-address-schema
  {:schema/alter-address-schema-10-8-16
   {:txes     [[{:db/id    :address/city
                 :db/ident :address/locality
                 :db/doc   "City/town"}
                {:db/id    :address/state
                 :db/ident :address/region
                 :db/doc   "State/province/region."}
                {:db/id                 #db/id[:db.part/db]
                 :db/ident              :address/country
                 :db/valueType          :db.type/string
                 :db/cardinality        :db.cardinality/one
                 :db/doc                "Country"
                 :db.install/_attribute :db.part/db}]]
    :requires [:starcity/add-address-schema
               :starcity/seed-gilbert
               :starcity/seed-mission
               :starcity/seed-union-square
               :starcity/seed-test-applications]}

   :seed/add-countries-to-addresses-10-8-16
   {:txes [(fn [conn]
             ;; Just find them all -- doesn't really matter which attr we use
             (let [addresses (find-all-by (d/db conn) :address/locality)]
               (mapv
                (fn [{id :db/id}]
                  [:db/add id :address/country "US"])
                addresses)))]
    :requires [:schema/alter-address-schema-10-8-16]}})
