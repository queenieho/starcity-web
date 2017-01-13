(ns starcity.api.admin.applications.entry
  (:require [clojure
             [spec :as s]
             [string :refer [split]]]
            [datomic.api :as d]
            [starcity
             [datomic :refer [conn]]
             [util :refer :all]]
            [starcity.api.common :as api]
            [starcity.models
             [account :as account]
             [application :as application]
             [income-file :as income-file]
             [property :as property]]))

;; =============================================================================
;; Internal
;; =============================================================================

(defn- parse-property [license property]
  {:id            (:db/id property)
   :name          (property/name property)
   :internal-name (property/internal-name property)
   :base-price    (property/base-rent property license)})

(defn- income-type [account]
  (if (empty? (income-file/by-account account)) "other" "file"))

(defn- parse-income [account]
  (letfn [(-parse-income-file [{:keys [:income-file/path :db/id]}]
            {:name (-> (split path #"/") last) :file-id id})]
    {:type  (income-type account)
     :files (map -parse-income-file (income-file/by-account account))}))

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
  (let [account (first (:account/_member-application application))]
    {:id                (:db/id application)
     :account-id        (:db/id account)
     :name              (account/full-name account)
     :move-in           (application/move-in-date application)
     :communities       (->> (application/communities application)
                             (map (partial parse-property (application/license application))))
     :term              (application/term application)
     :community-fitness (-> (application/community-fitness application)
                            (strip-namespaces))
     :income            (parse-income account)
     :address           (parse-address (application/address application))
     :pet               (parse-pet application)
     :status            (name (application/status application))
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
