(ns starcity.datomic.migrations.seed-union-square
  (:require [datomic.api :as d]
            [starcity.datomic.migration-utils :refer :all]))

(defn migration [conn]
  (let [pls [[1 2300.0] [6 2100.0] [12 2000.0]]]
    [{:db/id                    (d/tempid :db.part/starcity)
      :property/name            "Union Square"
      :property/description     "TODO:"
      :property/cover-image-url "/assets/img/union-square-cover.jpg"
      :property/upcoming        "Coming Q1 2017"
      :property/internal-name   "229ellis"
      :property/available-on    #inst "2017-02-01T00:00:00.000-00:00"
      :property/address         {:address/lines "229 Ellis St."
                                 :address/city  "San Francisco"}
      :property/licenses        (map (partial property-license conn) pls)
      :property/units           []}]))

(def norms
  {:starcity/seed-union-square {:txes     [migration]
                                :env      #{:development :staging}
                                :requires [:starcity/seed-licenses]}})
