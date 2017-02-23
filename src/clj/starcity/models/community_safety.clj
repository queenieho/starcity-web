(ns starcity.models.community-safety
  (:require [datomic.api :as d]
            [starcity.datomic :refer [conn tempid]]
            [starcity.models
             [account :as account]
             [address :as address]]
            [starcity.services.community-safety :as community-safety]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- perform-background-check!
  "Perform the background check using the service."
  [account]
  (letfn [(-maybe-assoc-middle-name [opts]
            (let [middle-name (account/middle-name account)]
              (if-not (empty? middle-name)
               (assoc opts :middle-name middle-name)
               opts)))
          (-maybe-assoc-address [opts]
            (if-let [address (-> account :account/application :application/address)]
              (assoc opts :address {:city        (address/city address)
                                    :state       (address/state address)
                                    :postal-code (address/zip address)})
              opts))]
    (community-safety/background-check (:db/id account)
                                       (account/first-name account)
                                       (account/last-name account)
                                       (account/email account)
                                       (account/dob account)
                                       (-> {}
                                           -maybe-assoc-middle-name
                                           -maybe-assoc-address))))

(def ^:private report-url-from
  (comp :location :headers))

(defn- create
  "Create a comfmunity safety entity."
  [account report-url]
  (let [ent {:community-safety/account       (:db/id account)
             :community-safety/report-url    report-url
             :community-safety/wants-report? true}
        tid (tempid)
        tx  @(d/transact conn [(assoc ent :db/id tid)])]
    (d/resolve-tempid (d/db conn) (:tempids tx) tid)))

(def ^:private check-successful?
  (comp (partial = 201) :status))

;; =============================================================================
;; API
;; =============================================================================

;; =============================================================================
;; Selectors

(def report :community-safety/report-url)

;; =============================================================================
;; Actions

(defn check!
  "Perform a community safety check on `account`."
  [account]
  (let [{:keys [status] :as result} (perform-background-check! account)]
    (if (check-successful? result)
      (create account (report-url-from result))
      (throw (ex-info "Failed to run background check!" {:status status})))))
