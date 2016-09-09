(ns starcity.spec
  (:require [clojure.spec :as s]))

(defn- date? [x]
  (= (type x) java.util.Date))

(s/def ::date date?)
(s/def ::basic-date (partial re-matches #"^\d{8}$"))

(s/def ::entity #(= (type %) datomic.query.EntityMap))

(s/def ::lookup
  (s/or :entity-id integer?
        :lookup-ref (s/and vector?
                           (s/cat :attr keyword?
                                  :val  (fn [x] (not (nil? x)))))))
