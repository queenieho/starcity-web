(ns migrations.seed-mission
  (:require [starcity.datomic.migration-utils :refer :all]
            [datomic.api :as d]))

(defn migration [conn]
  (let [pls [[1 2400.0] [6 2200.0] [12 2100.0]]]
    [{:db/id                    (d/tempid :db.part/starcity)
      :property/name            "The Mission"
      :property/description     "TODO:"
      :property/cover-image-url "/assets/img/mission-alley-cover.jpg"
      :property/internal-name   "2072mission"
      :property/available-on    #inst "2016-10-01T00:00:00.000-00:00"
      :property/address         {:address/lines "2072 Mission St."
                                 :address/city  "San Francisco"}
      :property/licenses        (map (partial property-license conn) pls)
      :property/units           []}]))

(def norms
  {:starcity/seed-mission {:txes [migration]
                           :env  #{:development :staging}
                           :required [:starcity/seed-licenses]}})
