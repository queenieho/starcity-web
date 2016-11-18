(ns starcity.datomic.migrations.add-member-application-status-11-15-16
  (:require [starcity.config.datomic :refer [partition]]
            [datomic.api :as d])
  (:refer-clojure :exclude [partition]))

(def add-member-application-status
  {:schema/add-member-application-status-11-15-16
   {:txes [(fn [_]
             [{:db/id                 #db/id[:db.part/db]
               :db/ident              :member-application/status
               :db/valueType          :db.type/ref
               :db/cardinality        :db.cardinality/one
               :db/doc                "The status of this member's application."
               :db.install/_attribute :db.part/db}

              {:db/id    (d/tempid partition)
               :db/ident :member-application.status/in-progress}
              {:db/id    (d/tempid partition)
               :db/ident :member-application.status/submitted}
              {:db/id    (d/tempid partition)
               :db/ident :member-application.status/approved}
              {:db/id    (d/tempid partition)
               :db/ident :member-application.status/rejected}])]
    :requires [:starcity/add-starcity-partition]}})

(defn- in-progress
  "Find all applications that are not locked and not approved."
  [conn]
  (->> (d/q '[:find ?e
              :where
              [_ :account/member-application ?e]
              (or [?e :member-application/locked false]
                  [(missing? $ ?e :member-application/locked)])
              (not [?e :member-application/approved true])]
            (d/db conn))
       (map first)))

(defn- submitted
  "Find all applications that are not approved and are locked."
  [conn]
  (->> (d/q '[:find ?e
              :where
              [?e :member-application/locked true]
              (not [?e :member-application/approved true])]
            (d/db conn))
       (map first)))

(defn- approved
  "Find all applications that are approved."
  [conn]
  (->> (d/q '[:find ?e
              :where
              [?e :member-application/approved true]]
            (d/db conn))
       (map first)))

(def seed-member-application-statuses-dev
  {:seed/seed-member-application-statuses-11-15-16
   {:txes     [(fn [conn]
                 (vec
                  (concat
                   (map (fn [e] [:db/add e :member-application/status :member-application.status/in-progress]) (in-progress conn))
                   (map (fn [e] [:db/add e :member-application/status :member-application.status/submitted]) (submitted conn))
                   (map (fn [e] [:db/add e :member-application/status :member-application.status/approved]) (approved conn)))))]
    :requires [:schema/add-member-application-status-11-15-16
               :starcity/add-account-schema
               :starcity/seed-test-applications
               :starcity/add-member-application-schema]}})

(def seed-member-application-statuses
  {:seed/seed-member-application-statuses-11-15-16
   {:txes     [(fn [conn]
                 (vec
                  (concat
                   (map (fn [e] [:db/add e :member-application/status :member-application.status/in-progress]) (in-progress conn))
                   (map (fn [e] [:db/add e :member-application/status :member-application.status/submitted]) (submitted conn))
                   (map (fn [e] [:db/add e :member-application/status :member-application.status/approved]) (approved conn)))))]
    :requires [:schema/add-member-application-status-11-15-16
               :starcity/add-account-schema
               :starcity/add-member-application-schema]}})
