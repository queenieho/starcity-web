(ns starcity.datomic.migrations.seed-test-applications-8-4-16
  (:require [starcity.config :refer [datomic]]
            [datomic.api :as d]))

(def seed-test-applications
  {:starcity/seed-test-applications
   {:txes     (let [application-id (d/tempid (:partition datomic))]
                [[{:db/id                                 application-id
                   :member-application/current-address    {:address/lines "7255 Wild Currant Way\nLower Floor"}
                   :member-application/desired-properties [[:property/internal-name "52gilbert"]]
                   }
                  {:account/member-application application-id
                   :db/id                      [:account/email "test@test.com"]}]])
    :requires [:starcity/seed-test-accounts
               :starcity/seed-gilbert]}})
