(ns starcity.datomic
  (:require [datomic.api :as d]
            [cpath-clj.core :as cp]
            [com.stuartsierra.component :as component]
            [taoensso.timbre :as timbre]
            [clojure.java.io :as io]
            [starcity.datomic.util :refer [qes]])
  (:import datomic.Util))

(timbre/refer-timbre)

;; ============================================================================
;; Schema

(defn- all-attrs
  [conn]
  (->> (qes '[:find ?a :where [_ :db.install/attribute ?a]]
            (d/db conn))
       (reduce #(conj %1 (d/touch %2)) [])))

(defn- new-idents
  [conn new]
  (letfn [(exists? [attr]
            (not-empty
             (filter #(= (:db/ident attr) (:db/ident %)) (all-attrs conn))))]
    (remove exists? new)))

(defn- read-edn
  [dir]
  (let [resources (cp/resources dir)]
    (->> (for [[filename _] resources]
           (with-open [f (-> (str dir filename) io/resource io/reader)]
             (Util/readAll f)))
         (flatten))))

(defn- install-schema
  "Ensure that any newly added attrs get transacted when the system is started."
  [conn dir]
  (let [schemas (read-edn dir)
        idents  (new-idents conn schemas ;; (remove #(nil? (:db/valueType %)) schemas)
                            )]
    (when-not (empty? idents)
      (debugf "Creating new attrs with idents: %s" (pr-str (map :db/ident idents)))
      @(d/transact conn (vec idents)))))

;; =============================================================================
;; Component

(defrecord Datomic [uri schema-dir seed-dir]
  component/Lifecycle
  (start [component]
    (debugf "Establishing Datomic Connection @ URI: %s" uri)
    (d/create-database uri)
    (let [conn (d/connect uri)]
      (install-schema conn schema-dir)
      (when seed-dir
        (debugf "Seeding database from %s" seed-dir)
        (try
          @(d/transact conn (vec (read-edn seed-dir))) ; hacky, but works!
          (catch Exception e
            (warn "Exception encountered while seeding database!" e))))
      (assoc component :conn conn :part :db.part/user))) ; move :part to config
  (stop [component]
    (debug "Closing Datomic Connection")
    (dissoc component :conn)))

(defn datomic
  [{:keys [uri schema-dir seed-dir]}]
  (map->Datomic {:schema-dir schema-dir :uri uri :seed-dir seed-dir}))
