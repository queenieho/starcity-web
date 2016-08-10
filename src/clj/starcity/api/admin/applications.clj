(ns starcity.api.admin.applications
  (:require [starcity.api.common :as common :refer [ok malformed]]
            [starcity.models.util :refer :all]
            [starcity.datomic :refer [conn]]
            [starcity.util :refer :all]
            [clojure.string :as str]
            [datomic.api :as d]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- strip-namespaces
  [m]
  (reduce
   (fn [acc [k v]]
     (assoc acc (keyword (name k)) v))
   {}
   m))

(defn- snake-keys
  [m]
  (reduce
   (fn [acc [k v]]
     (assoc acc (keyword (str/replace (name k) "-" "_")) v))
   {}
   m))

(def ^:private clean-map (comp snake-keys strip-namespaces))

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
      [:db/id :income-file/path]}
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

(defn- parse-plaid-income
  [{:keys [:plaid/income :plaid/bank-accounts]}]
  (let [income (first income)]
    (merge
     (clean-map (dissoc income :plaid-income/income-streams))
     {:streams  (map clean-map (:plaid-income/income-streams income))
      :accounts (map clean-map bank-accounts)})))

(defn- parse-income-file
  [{:keys [:income-file/path :db/id]}]
  {:name (-> (str/split path #"/") last) :file_id id})

(defn- parse-income
  [account]
  (if-let [plaid (-> account :plaid/_account first)]
    (merge (parse-plaid-income plaid) {:type "plaid"})
    {:type "file" :files (map parse-income-file (:income-file/_account account))}))

(defn- parse-application
  [{:keys [:account/_member-application :member-application/community-fitness] :as application}]
  (let [account   (first _member-application)]
    {:id                (:db/id application)
     :name              (parse-name account)
     :email             (:account/email account)
     :completed         (boolean (:member-application/locked application))
     :completed_at      (:member-application/submitted-at application)
     :community_fitness (clean-map community-fitness)
     :income            (parse-income account)}))

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

(defn fetch-application
  [{:keys [params] :as req}]
  (let [application-id (str->int (:application-id params))
        application    (d/pull (d/db conn) item-pattern application-id)]
    (ok (parse-application application))))

(defn fetch-income-file
  [{:keys [params] :as req}]
  (let [file-id (str->int (:file-id params))
        file    (d/entity (d/db conn) file-id)]
    (ring.util.response/file-response (:income-file/path file))))

(comment

  (fetch-application {:params {:application-id "285873023223094"}})

  )
