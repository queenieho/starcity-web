(ns starcity.datomic.migrations.initial.seed-gilbert
  (:require [starcity.datomic.migrations.utils :refer :all]
            [datomic.api :as d]))

(def seed-gilbert
  {:starcity/seed-gilbert
   {:txes [(fn [conn]
             (let [pls          [[1 2300.0] [6 2100.0] [12 2000.0]]
                   available-on #inst "2016-10-01T00:00:00.000-00:00"
                   units        (map #(assoc {:unit/available-on available-on
                                              :unit/floor        2}
                                             :db/id %)
                                     (tempids 5))]
               (concat
                units
                [{:db/id                    (d/tempid :db.part/starcity)
                  :property/name            "West SoMa"
                  :property/description     "A victorian home designed to be welcoming and enjoyable, our West SoMa community offers members 900 square feet in communal space. Members share a beautiful kitchen, social lounge, media room and more. Each member has their own private bedroom to nest in. The entire home is furnished to make moving in hassle-free. This community has five private units."
                  :property/cover-image-url "/assets/img/southpark-soma.jpg"
                  :property/internal-name   "52gilbert"
                  :property/available-on    available-on
                  :property/address         {:address/lines "52 Gilbert St."
                                             :address/city  "San Francisco"}
                  :property/licenses        (map (partial property-license conn) pls)
                  :property/units           (map :db/id units)}])))]
    :requires [:starcity/seed-licenses
               :starcity/add-unit-schema
               :starcity/add-property-schema
               :starcity/add-address-schema]}})
