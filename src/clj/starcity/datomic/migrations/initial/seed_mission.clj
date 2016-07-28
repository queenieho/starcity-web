(ns starcity.datomic.migrations.initial.seed-mission
  (:require [starcity.datomic.migrations.utils :refer :all]
            [starcity.datomic.migrations :refer [defnorms]]
            [datomic.api :as d]))

(defnorms seed-mission [conn]
  :txes (let [pls [[1 2400.0] [6 2200.0] [12 2100.0]]]
          [{:db/id                    (d/tempid :db.part/starcity)
            :property/name            "The Mission"
            :property/description     "TODO:"
            :property/cover-image-url "/assets/img/mission-alley-cover.jpg"
            :property/internal-name   "2072mission"
            :property/available-on    #inst "2016-10-01T00:00:00.000-00:00"
            :property/address         {:address/lines "2072 Mission St."
                                       :address/city  "San Francisco"}
            :property/licenses        (map (partial property-license conn) pls)
            :property/units           []}])
  :env  #{:development :staging}
  :required [seed-licenses add-property-schema add-address-schema])
