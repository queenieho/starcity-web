(ns starcity.datomic.migrations.utils
  (:require [starcity.models.util :refer [one]]
            [starcity.environment :as env]
            [starcity.config :refer [datomic]]
            [datomic.api :as d]
            [clojure.spec :as s]))

;; =============================================================================
;; Tempids

(defn add-tempids [txes]
  (map #(assoc % :db/id (d/tempid (:partition datomic))) txes))

(defn tempids [n]
  (repeatedly n (fn [] (d/tempid (:partition datomic)))))

;; =============================================================================
;; Environment Managment

(s/def ::migration map?)
(s/def ::environments
  (s/and set? #(every? #{:staging :production :development} %)))

(defn only-when [& args]
  (let [environment (first args)
        migrations  (rest args)]
    (if (environment env/environment)
      (apply merge migrations)
      {})))

(s/fdef only
        :args (s/cat :args (s/spec (s/cat :environments ::environments
                                          :migrations (s/spec (s/+ ::migration)))))
        :ret map?)

;; =============================================================================
;; Property Seeding

(defn license-id-for-term
  [conn term]
  (:db/id (one (d/db conn) :license/term term)))

(defn property-license
  [conn [term base-price]]
  {:property-license/license    (license-id-for-term conn term)
   :property-license/base-price base-price})
