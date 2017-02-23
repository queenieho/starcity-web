(ns tasks.pull-charge-amounts
  (:require [cheshire.core :as json]
            [clojure.core.async :as a :refer [<! chan go-loop put!]]
            [datomic.api :as d]
            [org.httpkit.client :as http]
            [plumbing.core :refer [assoc-when]]
            [starcity.datomic :refer [conn]]
            [starcity.services.codec :refer [form-encode]]
            [taoensso.timbre :as t]
            [toolbelt.predicates :refer [throwable?]]))

(def secret-key
  "")

(def ^:private base-url
  "https://api.stripe.com/v1")

(defn- log-error
  "Inspect the response and log errors if found."
  [{:keys [type message param]}]
  (t/error ::request-error {:type type :message message :param param}))

(defn- params-for
  [method params]
  (case method
    :get [:query-params params]
    [:body (form-encode params)]))

(defn- cb [c]
  (fn [{body :body}]
    (let [body (json/parse-string body true)]
      (if-let [e (:error body)]
        (do
          (log-error e)
          (put! c (ex-info "Error in request!" e)))
        (put! c body))
      (a/close! c))))

;; NOTE: This is a good baseline for a rewrite of our Stripe service.
;; TODO: Create a Stripe library
(defn- request
  [{:keys [endpoint method managed-account] :as conf} params]
  (let [req-map    {:url        (format "%s/%s" base-url endpoint)
                    :method     method
                    :headers    (assoc-when
                                 {"Accept" "application/json"}
                                 "Stripe-Account" managed-account)
                    :basic-auth [secret-key ""]}
        [k params] (params-for method params)]
    (let [c (chan 1)]
      (http/request (assoc req-map k params) (cb c))
      c)))

(defn- fetch-charge [charge-id]
  (request {:endpoint (format "charges/%s" charge-id)
            :method   :get}
           {}))

(defn- charges []
  (d/q '[:find [?s ...]
         :where
         [_ :charge/stripe-id ?s]]
       (d/db conn)))

(defn- fetch-amounts [charges]
  (let [in (a/merge (map fetch-charge charges))]
    (-> (a/reduce
         (fn [acc v]
           (if (throwable? v)
             (update acc :errors conj (ex-data v))
             (update acc :results conj [(:id v) (:amount v)])))
         {:results []
          :errors  []}
         ;; NOTE: not really doing anythign with the errors...poc?
         in)
        a/<!!
        :results)))

(defn- results->txes [results]
  (map (fn [[stripe-id amount]]
         {:db/id         [:charge/stripe-id stripe-id]
          :charge/amount (float (/ amount 100))})
       results))

(defn pull-charge-amounts []
  (->> (charges)
       fetch-amounts
       results->txes))

(comment

  (d/transact conn (pull-charge-amounts))

  (d/touch (d/entity (d/db conn) [:charge/stripe-id (first (charges))]))

  ;; A more imperative way to accomplish `fetch-amounts`
  (a/<!! (go-loop [acc {:errors  []
                        :results []}]
           (if-let [v (<! in)]
             (if (throwable? v)
               (recur (update acc :errors conj (ex-data v)))
               (recur (update acc :results conj [(:id v) (:amount v)])))
             acc)))
  )
