(ns starcity.config.community-safety
  (:require [starcity.config :refer [config]]
            [mount.core :refer [defstate]]))

(defstate community-safety :start (:community-safety config) :stop :noop)
(defstate api-key :start (:api-key community-safety) :stop :noop)
