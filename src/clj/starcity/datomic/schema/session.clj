(ns starcity.datomic.schema.session
  (:require [datomic-schema.schema :as s]))

(def ^{:added "1.1.4"} schema
  (s/generate-schema
   [(s/schema
     session
     (s/fields
      [key :string :unique-identity]
      [account :ref :index]
      [value :bytes :nohistory]))]))

(def norms
  {:schema/add-session-schema
   {:txes [schema]}})

;; (:something {:something (fn [x] (println x) x)} 5)
