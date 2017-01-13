(ns starcity.datomic.seed.approval
  (:require [datomic.api :as d]
            [starcity.datomic.partition :refer [tempid]]))

(defn seed [conn]
  @(d/transact conn [{:db/id                (tempid)
                      :approval/account     [:account/email "onboarding@test.com"]
                      :approval/approved-by [:account/email "admin@test.com"]
                      :approval/approved-on (java.util.Date.)
                      :approval/property    [:property/internal-name "52gilbert"]}
                     {:db/id                            (tempid)
                      :security-deposit/account         [:account/email "onboarding@test.com"]
                      :security-deposit/amount-required 2100}]))
