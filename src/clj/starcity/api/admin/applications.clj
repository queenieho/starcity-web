(ns starcity.api.admin.applications
  (:require [starcity.api.common :as api]
            [starcity.models.account :refer [full-name]]
            [starcity.models
             [application :as application]
             [approval :as approval]]
            [starcity.models.util :refer :all]
            [starcity.datomic :refer [conn]]
            [starcity.util :refer [str->int]]
            [clojure.string :as str]
            [datomic.api :as d]
            [clojure.spec :as s]))

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

(comment
  (defn- snake-keys
   [m]
   (reduce
    (fn [acc [k v]]
      (assoc acc (keyword (str/replace (name k) "-" "_")) v))
    {}
    m)))

(def ^:private clean-map strip-namespaces)

(def ^:private list-pattern
  [{:account/_member-application
    [:account/first-name
     :account/middle-name
     :account/last-name
     :account/phone-number
     :account/email
     :approval/_account]}
   {:member-application/desired-properties [:property/name]}
   {:member-application/desired-license [:license/term]}
   :member-application/desired-availability
   :member-application/locked
   :member-application/submitted-at
   :db/id])

(def ^:private item-pattern
  [{:account/_member-application
    [:db/id
     :account/first-name
     :account/middle-name
     :account/last-name
     :account/phone-number
     :account/email
     :approval/_account
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
    [:address/locality
     :address/postal-code
     :address/region
     :address/lines
     :address/country]}
   {:member-application/desired-properties [:property/name :property/internal-name]}
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

(defn- parse-plaid-income
  [{:keys [:plaid/income :plaid/bank-accounts]}]
  (let [income (first income)]
    (merge
     (clean-map (dissoc income :plaid-income/income-streams))
     {:streams  (map clean-map (:plaid-income/income-streams income))
      :accounts (map clean-map bank-accounts)})))

(defn- parse-income-file
  [{:keys [:income-file/path :db/id]}]
  {:name (-> (str/split path #"/") last) :file-id id})

(defn- parse-income
  [account]
  (if-let [plaid (-> account :plaid/_account first)]
    (merge (parse-plaid-income plaid) {:type "plaid"})
    {:type "file" :files (map parse-income-file (:income-file/_account account))}))

(defn- format-address
  [{:keys [:address/lines :address/locality :address/region :address/postal-code :address/country]}]
  (format "%s, %s %s, %s, %s" lines locality region postal-code country))

(defn- approved?
  [application]
  (not (nil? (get-in application [:account/_member-application 0 :approval/_account]))))

(defn- parse-application
  [{:keys [:account/_member-application :member-application/community-fitness] :as application}]
  (let [account (first _member-application)]
    {:id                (:db/id application)
     :name              (full-name account)
     :email             (:account/email account)
     :phone-number      (:account/phone-number account)
     :move-in           (:member-application/desired-availability application)
     :properties        (:member-application/desired-properties application)
     :term              (get-in application [:member-application/desired-license :license/term])
     :completed         (boolean (:member-application/locked application))
     :completed-at      (:member-application/submitted-at application)
     :community-fitness (clean-map community-fitness)
     :income            (parse-income account)
     :address           (format-address (:member-application/current-address application))
     :pet               (clean-map (:member-application/pet application))
     :approved          (approved? application)}))

;; =============================================================================
;; API
;; =============================================================================

;; =============================================================================
;; Queries

(defn fetch-applications
  "Fetch the list of applications. This is currently just ALL applications."
  []
  (letfn [(-parse-application [{:keys [:account/_member-application] :as application}]
            (let [account (first _member-application)]
              {:id           (:db/id application)
               :name         (full-name account)
               :email        (:account/email account)
               :phone-number (:account/phone-number account)
               :move-in      (:member-application/desired-availability application)
               :properties   (map :property/name (:member-application/desired-properties application))
               :term         (get-in application [:member-application/desired-license :license/term])
               :completed    (boolean (:member-application/locked application))
               :completed-at (:member-application/submitted-at application)
               :approved     (approved? application)}))]
    (let [ids (map :db/id (find-all-by (d/db conn) :member-application/locked true))]
      (->> (d/pull-many (d/db conn) list-pattern ids)
           (map -parse-application)
           (api/ok)))))

(defn fetch-application
  "Fetch a the application identified by `application-id`."
  [application-id]
  (let [application (d/pull (d/db conn) item-pattern application-id)]
    (api/ok (parse-application application))))

(s/fdef fetch-application
        :args (s/cat :application-id integer?))

;; =============================================================================
;; Approval

(defn- can-approve?
  [application-id]
  (and (not (application/approved? application-id))
       (application/locked? application-id)))

(defn approve
  "Approve the application identified by `application-id`."
  [application-id approver-id internal-name email-content]
  (cond
    (not (application/locked? application-id))
    (api/unprocessable {:error "The application is not yet complete, so cannot approve it."})

    (application/approved? application-id)
    (api/malformed {:error "This application has already been approved."})

    :otherwise (do
                 (approval/approve! application-id approver-id internal-name email-content)
                 (api/ok {:message "Success"}))))

(s/fdef approve
        :args (s/cat :application-id integer?
                     :approver-id integer?
                     :internal-name string?
                     :email-content string?))

;; =============================================================================
;; TODO: Move this to its own ns...doesn't belong here.

(defn fetch-income-file
  "Fetch an income file by `file-id` ."
  [file-id]
  (let [file (d/entity (d/db conn) file-id)]
    (ring.util.response/file-response (:income-file/path file))))

(s/fdef fetch-income-file
        :args (s/cat :file-id integer?))
