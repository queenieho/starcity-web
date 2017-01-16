(ns starcity.api.admin.accounts.security-deposit
  (:require [clj-time
             [coerce :as c]
             [format :as f]]
            [clojure.spec :as s]
            [compojure.core :refer [defroutes GET POST]]
            [datomic.api :as d]
            [starcity spec
             [datomic :refer [conn]]
             [util :refer [str->int transform-when-key-exists]]]
            [starcity.api.common :as api]
            [starcity.models
             [security-deposit :as security-deposit]
             [stripe :as stripe]]
            [starcity.models.check :as check]
            [starcity.util :refer :all]))

(s/def ::number integer?)
(s/def ::name string?)
(s/def ::status security-deposit/check-statuses)
(s/def ::amount float?)
(s/def ::date :starcity.spec/date)
(s/def ::received-on :starcity.spec/date)
(s/def ::bank string?)
(s/def ::id integer?)

(s/def ::check-data
  (s/keys :req-un [::number ::name ::status ::amount ::date ::received-on]
          :opt-un [::bank ::id]))

(def ^:private date-formatter (f/formatter "yyyy-MM-dd"))

(defn- transform-check-data
  [check-data]
  (let [to-date #(c/to-date (f/parse date-formatter %))]
    (transform-when-key-exists
        check-data
      {:status      #(keyword "check.status" %)
       :amount      float
       :date        to-date
       :received-on to-date})))

(defn- add-check!
  [account {:keys [status amount date received-on bank number name]}]
  (let [security-deposit (security-deposit/by-account account)
        check-tx (check/create name amount date number
                               :status status
                               :received-on received-on
                               :bank bank)]
    (println account security-deposit)
    [(security-deposit/add-check security-deposit check-tx)]))

(defn- update-check! [account check-id new-data]
  (let [check-tx         (-> (d/entity (d/db conn) check-id)
                             (check/update new-data))
        security-deposit (security-deposit/by-account account)]
    [check-tx (security-deposit/update-check security-deposit check-tx)]))

(defn- save [account check-data]
  (if-not (s/valid? ::check-data check-data)
    (api/malformed {:errors [(s/explain-str ::check-data check-data)]})
    (do
      (->> (if-let [check-id (:id check-data)]
             (update-check! account check-id check-data)
             (add-check! account check-data))
           (d/transact conn))
      (api/ok {}))))

(defn- fetch-charge [charge]
  (let [charge (stripe/fetch-charge charge)]
    (select-keys charge [:status :amount :id :created])))

(defn- clientize-check [check]
  {:id          (:db/id check)
   :name        (:check/name check)
   :bank        (:check/bank check)
   :amount      (:check/amount check)
   :number      (:check/number check)
   :date        (f/unparse date-formatter (c/to-date-time (:check/date check)))
   :received-on (f/unparse date-formatter (c/to-date-time (:check/received-on check)))
   :status      (name (:check/status check))})

(defn- fetch [account]
  (if-let [sd (security-deposit/by-account account)]
    (api/ok {:amount-received (get sd :security-deposit/amount-received 0)
             :amount-required (:security-deposit/amount-required sd)
             :payment-method  (when-let [method (:security-deposit/payment-method sd)]
                                (name method))
             :payment-type    (when-let [type (:security-deposit/payment-type sd)]
                                (name type))
             :charges         (map fetch-charge (:security-deposit/charges sd))
             :checks          (map clientize-check (:security-deposit/checks sd))})
    (api/ok {})))

(s/fdef fetch
        :args (s/cat :account entity?))

(defroutes routes
  (GET "/" [account-id]
       (fn [_]
         (fetch (d/entity (d/db conn) (str->int account-id)))))

  (POST "/check" [account-id]
        (fn [{params :params}]
          (save (d/entity (d/db conn) (str->int account-id))
                (transform-check-data params)))))
