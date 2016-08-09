(ns starcity.api.admin.applications
  (:require [starcity.api.common :as common :refer [ok malformed]]
            [starcity.models.util :refer :all]
            [starcity.datomic :refer [conn]]
            [starcity.util :refer :all]
            [datomic.api :as d]))

;; =============================================================================
;; Helpers
;; =============================================================================

;; (defn- strip-namespaces
;;   [data]
;;   )

(def ^:private list-pattern
  [{:account/_member-application
    [:account/first-name
     :account/middle-name
     :account/last-name
     :account/email]}
   :member-application/locked
   :member-application/submitted-at
   :db/id])

(def ^:private item-pattern
  [{:account/_member-application
                   [:db/id
                    :account/first-name
                    :account/middle-name
                    :account/last-name
                    :account/email
                    {:income-file/_account
                     [:income-file/content-type
                      :income-file/path]}
                    {:plaid/_account
                     [{:plaid/income [:plaid-income/last-year
                                      :plaid-income/last-year-pre-tax
                                      :plaid-income/projected-yearly
                                      {:plaid-income/income-streams
                                       [:income-stream/active
                                        :income-stream/confidence
                                        :income-stream/period
                                        :income-stream/income]}]}
                      {:plaid/bank-accounts
                       [:bank-account/available-balance
                        :bank-account/current-balance
                        :bank-account/type
                        :bank-account/subtype]}]}]}
                  {:member-application/current-address
                   [:address/city
                    :address/postal-code
                    :address/state]}
                  {:member-application/desired-properties [:property/name]}
                  {:member-application/desired-license [:license/term]}
                  {:member-application/pet
                   [:pet/breed :pet/type :pet/weight]}
                  {:member-application/community-fitness
                   [:community-fitness/why-interested
                    :community-fitness/prior-community-housing
                    :community-fitness/skills
                    :community-fitness/free-time
                    :community-fitness/dealbreakers]}
                  :member-application/locked
                  :member-application/submitted-at
                  :member-application/desired-availability
                  :db/id])

(defn- parse-name
  [{:keys [:account/first-name :account/last-name :account/middle-name]}]
  (if (not-empty middle-name)
    (format "%s %s %s" first-name middle-name last-name)
    (format "%s %s" first-name last-name)))

;; =============================================================================
;; API
;; =============================================================================

(defn fetch-applications
  [{:keys [params] :as req}]
  (letfn [(-parse-application [{:keys [:account/_member-application] :as application}]
            (let [account (first _member-application)]
              {:application_id (:db/id application)
               :name           (parse-name account)
               :email          (:account/email account)
               :completed      (boolean (:member-application/locked application))
               :completed_at   (:member-application/submitted-at application)}))]
    (let [ids (map :db/id (find-all-by (d/db conn) :member-application/locked true))]
     (->> (d/pull-many (d/db conn) list-pattern ids)
          (map -parse-application)
          (ok)))))

(fetch-applications {})


(defn fetch-application
  [{:keys [params] :as req}]
  (clojure.pprint/pprint req)
  (ok {:id (str->int (:application-id params))}))
