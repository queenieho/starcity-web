(ns starcity.controllers.application.logistics
  (:require [starcity.views.application.logistics :as view]
            [starcity.controllers.utils :refer :all]
            [starcity.models.property :as p]
            [starcity.models.application :as application]
            [starcity.datomic :refer [conn]]
            [starcity.datomic.util :refer :all]
            [datomic.api :as d]
            [clj-time.format :as f]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [ring.util.response :as response]
            [clojure.string :refer [lower-case trim]]
            [starcity.util :refer :all]
            [clojure.spec :as s]))

;; =============================================================================
;; Helpers
;; =============================================================================

;; =====================================
;; Date Formatters

(def ^:private value-formatter (f/formatter :basic-date))

;; =====================================
;; Helper Queries

(defn- property-exists? [property-id]
  (:property/name (one (d/db conn) property-id)))

(defn- get-availability
  [property-id simple-dates]
  (let [property (one (d/db conn) property-id)
        avail-m  (->> (p/units property)
                      (d/pull-many (d/db conn) [:unit/available-on])
                      (reduce (fn [acc {available :unit/available-on}]
                                (assoc acc (parse-date available value-formatter) available))
                              {}))]
    (map (partial get avail-m) simple-dates)))

(s/def ::get-availability
  (s/cat :property-id int?
         :simple-dates (s/spec (s/+ string?)))) ; TODO: better check than string?

(s/fdef get-availability
        :args ::get-availability
        :ret (s/* :starcity.spec/date))

(defn- application-for-account
  [account-id]
  (qe1 '[:find ?e :in $ ?u :where [?u :account/application ?e]]
       (d/db conn) account-id))

;; =====================================
;; Parameter Validation & Formatting

(defn- validate-params
  "Validate that the submitted logistics parameters are valid."
  [params]
  (letfn [(has-pet? [m] (= "yes" (:has-pet m)))
          (has-dog? [m] (and (has-pet? m) (= (get-in m [:pet :type]) "dog")))]
    (b/validate
     params
     {:availability               [(required "Availability must be indicated.")]
      :property-id                [(required "A property must be selected.")
                                   [property-exists? :message "That property is invalid."]]
      :selected-lease             [(required "A lease must be selected")]
      :has-pet                    [(required "A 'yes' or 'no' must be provided.")]
      :num-residents-acknowledged [(required "The per-unit resident limit must be acknowledged.")]
      [:pet :type]                [[v/required :message "A type of pet must be selected." :pre has-pet?]
                                   [v/member #{:dog :cat} :message "Only dogs and cats are allowed." :pre has-pet?]]
      [:pet :breed]               [[v/required :message "You must select a breed for your dog." :pre has-dog?]]
      [:pet :weight]              [[v/required :message "You must select a weight for your dog." :pre has-dog?]]})))

(defn- clean-params
  "Transform values in params to correct types and remove unnecessary
  information."
  [params]
  (letfn [(-has-pet? [params]
            (= (:has-pet params) "yes"))
          (-clean-values [params]
            (transform-when-key-exists params
              {:selected-lease str->int
               :property-id    str->int
               :pet            {:type   (comp keyword trim lower-case)
                                :id     str->int
                                :weight str->int
                                :breed  (comp trim lower-case)}}))
          (-clean-pet [params]
            (if (-has-pet? params)
              (-> (dissoc-when params [:pet :weight] nil?)
                  (dissoc-when [:pet :breed] empty?))
              (dissoc params :pet)))]
    (-> (-clean-values params) (-clean-pet))))

;; =====================================
;; Misc.

(defn- show-logistics*
  [{:keys [params identity] :as req} & {:keys [errors] :or {errors []}}]
  (let [property    (one (d/db conn) :property/internal-name "alpha")
        application (application-for-account (:db/id identity))]
    (view/logistics application property errors)))

;; =============================================================================
;; API
;; =============================================================================

(defn show-logistics
  [req]
  (ok (show-logistics* req)))

(defn save! [{:keys [params form-params] :as req}]
  (let [vresult    (-> params clean-params validate-params)
        account-id (get-in req [:identity :db/id])]
    (if-let [{:keys [selected-lease pet availability property-id]} (valid? vresult)]
      (let [desired-availability (get-availability property-id availability)]
        ;; If there is an existing rental application for this user, we're
        ;; dealing with an update.
        (if-let [existing (application/by-account-id account-id)]
          (application/update! (:db/id existing)
                               {:desired-lease        selected-lease
                                :desired-availability desired-availability
                                :pet                  pet})
          ;; otherwise, we're creating a new rental application for this user.
          (application/create! account-id selected-lease desired-availability :pet pet))
        ;; afterwards, redirect back to the "/application" endpoint
        (response/redirect "/application"))
      ;; results aren't valid! indicate errors
      (malformed (show-logistics* req :errors (errors-from vresult))))))
