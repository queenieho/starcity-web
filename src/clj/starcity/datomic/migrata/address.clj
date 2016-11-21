(ns starcity.datomic.migrata.address
  (:require [datomic.api :as d]))

(defn- add-countries-to-addresses
  "Prior to adding international support, there was no `:address/country`
  attribute; however, due to lack of international support, all accounts must
  have been in the US."
  {:added "1.0.x"}
  [conn]
  (->> (d/q '[:find [?a ...]
              :where
              [?a :address/locality _]
              [(missing? $ ?a :address/country)]]
            (d/db conn))
       (mapv #([:db/add % :address/country "US"]))))

(defn norms [conn]
  {:seed/add-countries-to-addresses-10-8-16
   {:txes [(add-countries-to-addresses conn)]}})
