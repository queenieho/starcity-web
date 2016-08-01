(ns starcity.datomic.migrations.initial.seed-historic-tenderloin
  (:require [starcity.datomic.migrations.utils :refer :all]
            [starcity.datomic.migrations :refer [defnorms]]
            [datomic.api :as d]))

(defnorms seed-historic-tenderloin [conn]
  :txes (let [pls          [[1 2300.0] [6 2100.0] [12 2000.0]]
              available-on #inst "2018-01-01T00:00:00.000-00:00"
              units        (map (fn [id] {:db/id id :unit/available-on available-on})
                                (tempids 200))]
          (concat
           units
           [{:db/id                    (d/tempid :db.part/starcity)
             :property/name            "Historic Tenderloin"
             :property/description     "This is a ground-up development in the historic Tenderloin neighborhood. It will offer 231 new housing units with lots of communal space. All units will have state of the art private baths and cooking facilities. Units will be approximately 250 square feet, 350 square feet, and 450 square feet. This project will be two, seven-story buildings adjacent to each other with 5,000 square feet of retail space on the ground. There will be shared roof decks and double height lounges on every other floor. We are implementing state of the art green technologies with very low energy and water costs. Sign up now to have a say in the development of communal areas and to engage with early community members!"
             :property/cover-image-url "/assets/img/tenderloin-cover-image.jpeg"
             :property/upcoming        "Q4 2017"
             :property/internal-name   "361turk"
             :property/available-on    available-on
             :property/address         {:address/lines "361 Turk St."
                                        :address/city  "San Francisco"}
             :property/licenses        (map (partial property-license conn) pls)
             :property/units           (map :db/id units)}]))
  :requires [seed-licenses add-unit-schema add-property-schema add-address-schema])
