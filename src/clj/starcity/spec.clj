(ns starcity.spec
  (:require [clojure.spec :as s]))

(defn- date? [x]
  (println (type x))
  (= (type x) java.util.Date))

(s/def ::date date?)
(s/def ::basic-date (partial re-matches #"^\d{8}$"))
