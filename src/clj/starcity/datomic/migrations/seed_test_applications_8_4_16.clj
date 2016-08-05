(ns starcity.datomic.migrations.seed-test-applications-8-4-16
  (:require [starcity.config :refer [datomic]]
            [starcity.models.util :refer [one]]
            [datomic.api :as d]))

(def seed-test-applications
  {:starcity/seed-test-applications
   {:txes     [(fn [conn]
                 (let [application-id (d/tempid (:partition datomic))]
                   [{:db/id                                   application-id
                     :member-application/current-address      {:address/lines       "7255 Wild Currant Way\nLower Floor"
                                                               :address/city        "Oakland"
                                                               :address/state       "CA"
                                                               :address/postal-code "94611"}
                     :member-application/desired-properties   [[:property/internal-name "52gilbert"]
                                                               [:property/internal-name "229ellis"]]
                     :member-application/desired-license      (:db/id (one (d/db conn) :license/term 6))
                     :member-application/locked               true
                     :member-application/desired-availability (java.util.Date.)
                     :member-application/community-fitness    {:community-fitness/why-interested          "Because."
                                                               :community-fitness/prior-community-housing "Dorms."
                                                               :community-fitness/skills                  "Emacs. Codez."
                                                               :community-fitness/free-time               "Photography."
                                                               :community-fitness/dealbreakers            "Stupidity."}
                     :member-application/submitted-at         (java.util.Date.)
                     ;; TODO:
                     }
                    {:account/member-application application-id
                     :db/id                      [:account/email "test@test.com"]}
                    {:db/id               (d/tempid (:partition datomic))
                     :plaid/account       [:account/email "test@test.com"]
                     :plaid/income        [{:plaid-income/last-year         50000
                                            :plaid-income/last-year-pre-tax 70000
                                            :plaid-income/projected-yearly  80000
                                            :plaid-income/income-streams
                                            [{:income-stream/active     true
                                              :income-stream/confidence 1.0
                                              :income-stream/period     15
                                              :income-stream/income     6666}
                                             {:income-stream/active     true
                                              :income-stream/confidence 1.0
                                              :income-stream/period     15
                                              :income-stream/income     6666}]}]
                     :plaid/bank-accounts [{:bank-account/available-balance 4000.0
                                            :bank-account/current-balance   3889.0
                                            :bank-account/type              "depository"
                                            :bank-account/subtype           "checking"
                                            :bank-account/institution-type  "wells"}
                                           {:bank-account/available-balance 4000.0
                                            :bank-account/current-balance   3889.0
                                            :bank-account/type              "depository"
                                            :bank-account/subtype           "checking"
                                            :bank-account/institution-type  "wells"}]}]))]
    :requires [:starcity/seed-test-accounts
               :starcity/seed-gilbert]}})
