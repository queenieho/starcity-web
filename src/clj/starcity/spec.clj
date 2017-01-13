(ns starcity.spec
  (:require [clojure.spec :as s]
            [clojure.string :as st]))

;; =============================================================================
;; Internal

(defn- date? [x]
  (= (type x) java.util.Date))

(defn- datetime? [x]
  (= (type x) org.joda.time.DateTime))

;; =============================================================================
;; Dates

(s/def ::instant date?)
(s/def ::date date?)
(s/def ::datetime datetime?)
(s/def ::basic-date (partial re-matches #"^\d{8}$"))

;; =============================================================================
;; Datomic

(s/def ::entity #(= (type %) datomic.query.EntityMap))
(s/def ::lookup
  (s/or :entity-id integer?
        :lookup-ref (s/and vector?
                           (s/cat :attr keyword?
                                  :val  (fn [x] (not (nil? x)))))))

;; =============================================================================
;; Misc

(s/def ::non-empty-string (comp not st/blank?))
