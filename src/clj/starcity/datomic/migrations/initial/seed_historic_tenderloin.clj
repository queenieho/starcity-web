(ns starcity.datomic.migrations.initial.seed-historic-tenderloin
  (:require [starcity.datomic.migrations.utils :refer :all]
            [starcity.datomic.migrations :refer [defnorms]]
            [datomic.api :as d]))

(defnorms seed-historic-tenderloin [conn]
  :txes (let [pls [[1 2300.0] [6 2100.0] [12 2000.0]]]
          [{:db/id                    (d/tempid :db.part/starcity)
            :property/name            "Historic Tenderloin"
            :property/description     "TODO:"
            :property/cover-image-url "/assets/img/tenderloin-cover-image.jpeg"
            :property/upcoming        "Coming Q4 2017"
            :property/internal-name   "361turk"
            :property/available-on    #inst "2018-01-01T00:00:00.000-00:00"
            :property/address         {:address/lines "361 Turk St."
                                       :address/city  "San Francisco"}
            :property/licenses        (map (partial property-license conn) pls)
            :property/units           []}])
  :env      #{:development :staging}
  :requires [seed-licenses add-property-schema add-address-schema])
