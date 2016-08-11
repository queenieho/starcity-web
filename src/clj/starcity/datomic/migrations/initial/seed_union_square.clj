(ns starcity.datomic.migrations.initial.seed-union-square
  (:require [starcity.datomic.migrations.utils :refer :all]
            [datomic.api :as d]))

(def seed-union-square
  {:starcity/seed-union-square
   {:txes [(fn [conn]
             (let [pls          [[1 2300.0] [6 2100.0] [12 2000.0]]
                   available-on #inst "2017-02-01T00:00:00.000-00:00"
                   units        (map (fn [id] {:db/id id :unit/available-on available-on})
                                     (tempids 56))]
               (concat
                units
                [{:db/id                    (d/tempid :db.part/starcity)
                  :property/name            "Union Square"
                  :property/description     "Our Union Square location is a four-story, masonry structure completed in 1910. The building was built as a bathhouse and continued to be used as such until the mid-1980's. We're converting the top three floors into private units for our members and the ground-level offerings will be determined by the initial members of the community. Sign up now to be a part of this communal building and have a say on the function of communal spaces! This community has room for 40 private units."
                  :property/cover-image-url "/assets/img/union-square-cover.jpg"
                  :property/upcoming        "Q1 2017"
                  :property/internal-name   "229ellis"
                  :property/available-on    available-on
                  :property/address         {:address/lines "229 Ellis St."
                                             :address/city  "San Francisco"}
                  :property/licenses        (map (partial property-license conn) pls)
                  :property/units           (map :db/id units)}])))]
    :requires [:starcity/seed-licenses
               :starcity/add-unit-schema
               :starcity/add-property-schema
               :starcity/add-address-schema]}})
