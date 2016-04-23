(ns starcity.datomic
  (:require [datomic.api :as d]
            [starcity.datomic.utils :refer :all]
            [me.raynes.fs :as fs]
            [com.stuartsierra.component :as component]
            [taoensso.timbre :as timbre])
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

(defn- read-all-schemas
  [dir]
  (->> (for [f (fs/list-dir dir)]
         (with-open [f (clojure.java.io/reader f)]
           (Util/readAll f)))
       (flatten)))

(defn- install-schema
  "Ensure that any newly added attrs get transacted when the system is started."
  [conn dir]
  (let [schemas (read-all-schemas dir)
        idents (new-idents conn (remove #(nil? (:db/valueType %)) schemas))]
    (when-not (empty? idents)
      (debug ";; Creating new attrs with idents: " (map :db/ident idents))
      @(d/transact conn (vec idents)))))

;; =============================================================================
;; Component

(defrecord Datomic [uri schema-dir]
  component/Lifecycle
  (start [component]
    (debugf "Establishing Datomic Connection @ URI: %s" uri)
    (d/create-database uri)
    (let [conn (d/connect uri)]
      (install-schema conn schema-dir)
      (assoc component :connection conn)))
  (stop [component]
    (debug "Closing Datomic Connection")
    (dissoc component :connection)))

(defn datomic
  [{:keys [uri schema-dir]}]
  (map->Datomic {:schema-dir schema-dir :uri uri}))
