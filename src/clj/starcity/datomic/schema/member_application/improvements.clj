(ns starcity.datomic.schema.member-application.improvements
  "A variety of improvements to the application schema that add indices to all
  attributes, improve naming, and fix some mistakes.")

(def ^{:added "1.1.4"} schema
  [{:db/id               :member-application/desired-properties
    :db/ident            :member-application/properties
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :member-application/desired-license
    :db/ident            :member-application/license
    :db/index            true
    :db/isComponent      false
    :db.alter/_attribute :db.part/db}
   {:db/id               :member-application/desired-availability
    :db/ident            :member-application/move-in
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id  :member-application/locked
    :db/doc "DEPRECATED in favor of :member-application/status, 11/20/16"}
   {:db/id  :member-application/approved
    :db/doc "DEPRECATED in favor of :member-application/status, 11/20/16"}
   {:db/id  :member-application/submitted-at
    :db/doc "DEPRECATED 11/20/16"}])
