(ns starcity.api.admin.applications.entry
  (:require [datomic.api :as d]
            [starcity.util :refer :all]
            [clojure.spec :as s]
            [clojure.string :refer [split]]
            [starcity.datomic :refer [conn]]
            [starcity.api.common :as api]
            [starcity.models.account :as account]
            [starcity.models.application :as application]
            [starcity.models.property :as property]
            [starcity.models.approval :as approval]))

;; =============================================================================
;; Internal
;; =============================================================================

(defn- parse-property [license property]
  {:id            (:db/id property)
   :name          (property/name property)
   :internal-name (property/internal-name property)
   :base-price    (property/base-rent property license)})

(defn- income-type [account]
  (if (empty? (account/income-files account)) "other" "file"))

(defn- parse-income [account]
  (letfn [(-parse-income-file [{:keys [:income-file/path :db/id]}]
            {:name (-> (split path #"/") last) :file-id id})]
    {:type  (income-type account)
     :files (map -parse-income-file (account/income-files account))}))

(defn- parse-address
  [{:keys [:address/lines
           :address/locality
           :address/region
           :address/postal-code
           :address/country]}]
  (format "%s, %s %s, %s, %s" lines locality region postal-code (or country "US")))

(defn- parse-pet
  [application]
  (assoc (strip-namespaces (application/pet application))
         :has-pet (application/has-pet? application)))

(defn- parse [application]
  (let [account (account/by-application application)]
    {:id                (:db/id application)
     :account-id        (:db/id account)
     :name              (account/full-name account)
     :move-in           (application/move-in-date application)
     :communities       (->> (application/communities application)
                             (map (partial parse-property (application/license application))))
     :term              (application/term application)
     :community-fitness (-> (application/community-fitness application)
                            (strip-namespaces))
     :income            (parse-income application)
     :address           (parse-address (application/address application))
     :pet               (parse-pet application)
     :approved          (application/approved? application)
     :completed         (application/completed? application)
     :completed-at      (application/completed-at application)}))

;; =============================================================================
;; API
;; =============================================================================

(defn fetch
  "Fetch the member application identified by `application-id`."
  [application-id]
  (-> (d/entity (d/db conn) application-id)
      (parse)
      (api/ok)))

(s/fdef fetch
        :args (s/cat :application-id integer?))
