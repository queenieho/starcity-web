(ns starcity.datomic.seed
  (:require [starcity.datomic.seed.staging :as staging]))

;; TODO: Restructure seeds to live in a separate directory that can be
;; selectively included in the resulting jar based on path...somehow.
(def ^:private norms
  {:staging staging/norms})

(defn seed [conn env]
  (when-let [conform (get norms env)]
    (conform conn)))
