(ns starcity.models.apply.progress
  (:require [clojure.spec :as s]
            [datomic.api :as d]
            [plumbing.core :refer [assoc-when]]
            [starcity spec
             [datomic :refer [conn]]]
            [starcity.models.application :as application]))

;; =============================================================================
;; Internal
;; =============================================================================

;; TODO: Use Entity API
(defn- fetch
  [account-id]
  (d/pull (d/db conn)
          [:account/first-name
           :account/last-name
           :account/middle-name
           :account/phone-number
           :account/dob
           {:income-file/_account [:income-file/path]}
           {:community-safety/_account
            [:db/id :community-safety/consent-given?]}
           {:account/application
            [{:application/communities
              [:property/internal-name]}
             {:application/license [:db/id]}
             :application/move-in
             :application/has-pet
             {:application/pet
              [:pet/type :pet/breed :pet/weight]}
             {:application/address
              [:address/region :address/locality :address/postal-code :address/country]}
             {:application/fitness
              [:fitness/interested
               :fitness/free-time
               :fitness/dealbreakers
               :fitness/skills
               :fitness/experience]}
             :application/status
             :db/id]}]
          account-id))

;; =====================================
;; Account

(defn- parse-account
  [{:keys [:account/first-name
           :account/last-name
           :account/middle-name
           :account/phone-number
           :account/dob]}]
  {:account
   (-> {:name {:first  first-name
               :middle middle-name
               :last   last-name}}
       (assoc-when :phone-number phone-number
                   :dob dob))})

;; =====================================
;; Communities

(defn- parse-communities [data]
  (let [communities (get-in data [:account/application
                                  :application/communities])]
    {:communities (map :property/internal-name communities)}))

;; =====================================
;; license

(defn- parse-license [data]
  {:license (get-in data [:account/application
                          :application/license
                          :db/id])})

;; =====================================
;; move-in date

(defn- parse-move-in [data]
  {:move-in-date (get-in data [:account/application
                               :application/move-in])})

;; =====================================
;; Pet

;; TODO: Why not use `:member-application/has-pet`?
(defn- parse-pet [data]
  (let [{:keys [:pet/type :pet/breed :pet/weight] :as pet}
        (get-in data [:account/application
                      :application/pet])]
    (if (nil? (get-in data [:account/application :application/has-pet]))
      {}
      {:pet (assoc-when {:has-pet (boolean pet)}
                        :pet-type (when type (name type))
                        :breed breed
                        :weight weight)})))

;; =====================================
;; Background

(defn- parse-community-safety [data]
  {:community-safety
   {:consent (get-in data [:community-safety/_account
                           0
                           :community-safety/consent-given?])}})

;; =====================================
;; Address

(defn- parse-address [data]
  (let [address (get-in data [:account/application
                              :application/address])]
    {:address {:locality    (:address/locality address)
               :region      (:address/region address)
               :postal-code (:address/postal-code address)
               :country     (:address/country address)}}))

;; =====================================
;; Income Files

(defn- parse-income-files [data]
  (let [files (:income-file/_account data)]
    {:income-file-paths (map :income-file/path files)}))

;; =====================================
;; Community Fitness

(defn- parse-fitness [data]
  {:fitness
   (get-in data [:account/application
                 :application/fitness])})

;; =====================================
;; Completion

(defn- parse-completion [data]
  (let [application (d/entity (d/db conn)
                              (get-in data [:account/application :db/id]))]
    {:complete (and application
                    (not (application/in-progress? application)))}))

;; =====================================
;; Completeness

;; NOTE: Using spec for this since it's quick. Might not be the best.
;; I'm really just checking types of the resulting "progress", but the big
;; assumption here is that all of the data was properly validated before going
;; into the database. Since this validation is only of data that's come out of
;; the db, it shouldn't need another serious validation pass.

(s/def ::communities (s/+ string?))
(s/def ::license integer?)
(s/def ::move-in-date :starcity.spec/date)
;; pet
(s/def ::weight integer?)
(s/def ::breed string?)
(s/def ::pet-type #{"cat" "dog"})
(s/def ::has-pet boolean?)
(s/def ::pet (s/keys :req-un [::has-pet] :opt-un [::pet-type ::breed ::weight]))
;; background
(s/def ::consent true?)
(s/def ::community-safety (s/keys :req-un [::consent]))
;; address
;; TODO: This causes a problem. Why is that? Probably because the s/def happens
;; before the state has mounted
;; (s/def ::country countries/codes)
(s/def ::locality :starcity.spec/non-empty-string)
(s/def ::region :starcity.spec/non-empty-string)
(s/def ::postal-code :starcity.spec/non-empty-string)
(s/def ::address (s/keys :req-un [::locality ::region ::postal-code ::country]))

(s/def ::income-file-paths (s/+ string?))
;; Community fitness
(s/def :fitness/free-time :starcity.spec/non-empty-string)
(s/def :fitness/skills :starcity.spec/non-empty-string)
(s/def :fitness/experience :starcity.spec/non-empty-string)
(s/def :fitness/interested :starcity.spec/non-empty-string)
(s/def :fitness/dealbreakers :starcity.spec/non-empty-string)
(s/def ::fitness
  (s/keys :req [:fitness/free-time
                :fitness/skills
                :fitness/experience
                :fitness/interested]
          :opt [:fitness/dealbreakers]))

(s/def ::complete-parsed-data
  (s/keys :req-un [::communities ::license ::move-in-date ::community-safety
                   ::address ::income-file-paths ::fitness ::pet]))

;; =============================================================================
;; API
;; =============================================================================

(def is-payment-allowed?
  (partial s/valid? ::complete-parsed-data))

(defn progress [account-id]
  (let [data (fetch account-id)
        res  (apply merge ((juxt parse-account
                                 parse-communities
                                 parse-license
                                 parse-move-in
                                 parse-pet
                                 parse-community-safety
                                 parse-address
                                 parse-income-files
                                 parse-fitness
                                 parse-completion)
                           data))
        res (assoc res :payment-allowed (is-payment-allowed? res))]
    res))
