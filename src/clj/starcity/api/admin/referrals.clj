(ns starcity.api.admin.referrals
  (:require [blueprints.models.account :as account]
            [clj-time.coerce :as c]
            [compojure.core :refer [defroutes GET]]
            [datomic.api :as d]
            [plumbing.core :as plumbing]
            [starcity.datomic :refer [conn]]
            [starcity.util.response :as response]))

(defn- format-params [params]
  (-> (plumbing/update-in-when params [:pstart] c/to-date)
      (plumbing/update-in-when [:pend] c/to-date)))

(defn- query-referrals [db pstart pend]
  (d/q '[:find ?e ?created
         :in $ ?pstart ?pend
         :where
         [?e :referral/source _ ?tx]
         [?tx :db/txInstant ?created]
         [(.after ^java.util.Date ?created ?pstart)]
         [(.before ^java.util.Date ?created ?pend)]]
       db pstart pend))

(defn- clientize [db [referral created]]
  (let [referral (d/entity db referral)]
    (merge
     {:source  (:referral/source referral)
      :from    (:referral/from referral)
      :created created}
     (when-let [account (:referral/account referral)]
       {:account {:id    (:db/id account)
                  :email (account/email account)
                  :name  (account/full-name account)}}))))

(defroutes routes
  (GET "/" []
       (fn [{params :params}]
         (let [{:keys [pstart pend]} (format-params params)
               db                    (d/db conn)]
           (if (and pstart pend)
             (response/transit-ok {:result (->> (query-referrals db pstart pend)
                                                (map (partial clientize db)))})
             (response/transit-malformed {:errors ["Please specify the period start and end date."]}))))))

(comment
  ;; Add some sample referrals for debugging
  @(d/transact conn [{:db/id            (d/tempid :db.part/starcity)
                      :referral/source  "word of mouth"
                      :referral/from    :referral.from/tour
                      :referral/account [:account/email "test@test.com"]}
                     {:db/id            (d/tempid :db.part/starcity)
                      :referral/source  "instagram"
                      :referral/from    :referral.from/tour
                      :referral/account [:account/email "member@test.com"]}
                     {:db/id            (d/tempid :db.part/starcity)
                      :referral/source  "instagram"
                      :referral/from    :referral.from/tour}])

  )
