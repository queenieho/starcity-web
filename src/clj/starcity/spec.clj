(ns starcity.spec
  (:require [clojure.spec :as s]
            [clj-time.core :as t]))

(defn- date? [x]
  (= (type x) java.util.Date))

(defn- datetime? [x]
  (= (type x) org.joda.time.DateTime))

(s/def ::date date?)
(s/def ::datetime datetime?)
(s/def ::basic-date (partial re-matches #"^\d{8}$"))

(s/def ::entity #(= (type %) datomic.query.EntityMap))

(s/def ::lookup
  (s/or :entity-id integer?
        :lookup-ref (s/and vector?
                           (s/cat :attr keyword?
                                  :val  (fn [x] (not (nil? x)))))))
