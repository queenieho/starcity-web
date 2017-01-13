(ns starcity.spec.datomic
  "App-wide specs related to Datomic ops."
  (:require [clojure.spec :as s]))

(defn- type= [t]
  #(= (type %) t))

(s/def ::entity (type= datomic.query.EntityMap))
(s/def ::db (type= datomic.db.Db))
(s/def ::connection (type= datomic.peer.LocalConnection))

(s/def ::lookup
  (s/or :entity-id integer?
        :lookup-ref (s/and vector?
                           (s/cat :attr keyword?
                                  :val  (fn [x] (not (nil? x)))))))
