(ns starcity.tasks.pull-applications
  (:require [datomic.api :as d]
            [hiccup.core :refer [html]]
            [starcity.datomic :refer [conn]]
            [starcity.models.util :refer :all]
            [starcity.services.mailgun :refer [send-email]]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- pull-applications
  []
  (let [ids (map :db/id (find-all-by (d/db conn) :member-application/locked true))]
    (d/pull-many (d/db conn)
                 [{:account/_member-application
                   [:account/first-name
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
                  :member-application/submitted-at
                  :member-application/desired-availability]
                 ids)))

(defn- transform-application
  [application]
  (-> application
      (update-in [:account/_member-application] first)
      (update-in [:account/_member-application :plaid/_account] first)))

(defn- format-map
  [m initial]
  (reduce
   (fn [acc [k v]]
     (conj acc
           [:li
            [:p [:b k]
             (cond
               (map? v)    (format-map v [:ul])
               (vector? v) (map #(format-map % [:ol]) v)
               :otherwise  (format ": %s" v))]]))
   initial
   m))

(defn- format-application
  [application]
  (let [name (format "%s %s"
                     (get-in application [:account/_member-application :account/first-name])
                     (get-in application [:account/_member-application :account/last-name]))]
    (html
     [:section
      [:div
       [:h3 name]
       (format-map application [:ul])]])))

(defn- format-applications
  [applications]
  (map (comp format-application transform-application) applications))

;; =============================================================================
;; API
;; =============================================================================

(defn send-applications
  [& recipients]
  (let [content (format-applications (pull-applications))]
    (doseq [to recipients]
      (send-email to "Completed Applications" content))))
