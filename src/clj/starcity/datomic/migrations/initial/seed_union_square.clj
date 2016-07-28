(ns starcity.datomic.migrations.initial.seed-union-square
  (:require [starcity.datomic.migrations :refer [defnorms]]
            [starcity.datomic.migrations.utils :refer :all]
            [datomic.api :as d]))

(defnorms seed-union-square [conn]
  :txes (let [pls [[1 2300.0] [6 2100.0] [12 2000.0]]]
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
            :property/units           []}])
  :env      #{:development :staging}
  :requires [seed-licenses add-property-schema add-address-schema])
