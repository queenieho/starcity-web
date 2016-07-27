(ns starcity.datomic.migrations.seed-gilbert
  (:require [datomic.api :as d]
            [starcity.datomic.migration-utils :refer :all]))

(defn migration
  [conn]
  (let [pls [[1 2300.0] [6 2100.0] [12 2000.0]]]
    [{:db/id                    (d/tempid :db.part/starcity)
      :property/name            "West SoMa"
      :property/description     "A victorian home designed to be welcoming and enjoyable, Gilbert offers members over 900 square feet in Community space. Members share a beautiful kitchen, lounge, media room and more. Each member has their own private bedroom to nest in. The entire home is furnished to make moving in hassle-free."
      :property/cover-image-url "/assets/img/southpark-soma.jpg"
      :property/internal-name   "52gilbert"
      :property/available-on    #inst "2016-09-15T00:00:00.000-00:00"
      :property/address         {:address/lines "52 Gilbert St."
                                 :address/city  "San Francisco"}
      :property/licenses        (map (partial property-license conn) pls)
      :property/units           []}]))

(def norms
  {:starcity/seed-gilbert {:txes     [migration]
                           :env      #{:development :staging}
                           :requires [:starcity/seed-licenses]}})
