(ns starcity.datomic.migrations.initial.seed-mission
  (:require [starcity.datomic.migrations.utils :refer :all]
            [starcity.datomic.migrations :refer [defnorms]]
            [datomic.api :as d]))

(defnorms seed-mission [conn]
  :txes (let [pls [[1 2400.0] [6 2200.0] [12 2100.0]]]
          [{:db/id                    (d/tempid :db.part/starcity)
            :property/name            "The Mission"
            :property/description     "In the heart of the Mission neighborhood, we've converted this three-story building to offer our community members over 1,000 square feet in communal space. Members share an industrial-sized kitchen with multiple cooking stations, a bar area, social lounges, media room and back storage. Members also have access to a back patio to soak in some sunshine. Each member gets their own private bedroom for rest and relaxation. As with all of our communities, all common and private areas are furnished for easy move-in. This community has 20 private units."
            :property/cover-image-url "/assets/img/mission-alley-cover.jpg"
            :property/internal-name   "2072mission"
            :property/available-on    #inst "2016-10-01T00:00:00.000-00:00"
            :property/address         {:address/lines "2072 Mission St."
                                       :address/city  "San Francisco"}
            :property/licenses        (map (partial property-license conn) pls)
            :property/units           []}])
  :env  #{:development :staging}
  :required [seed-licenses add-property-schema add-address-schema])
