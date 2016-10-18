(ns starcity.config.datomic
  (:require [starcity.config :refer [config]]
            [mount.core :as mount :refer [defstate]])
  (:refer-clojure :exclude [partition]))

(defstate datomic :start (:datomic config) :stop :noop)
(defstate partition :start (:partition datomic) :stop :noop)
