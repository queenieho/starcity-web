(ns starcity.config.slack
  (:require [starcity.config :refer [config]]
            [mount.core :refer [defstate]]))

(defstate slack :start (:slack config) :stop :noop)
(defstate webhook-url :start (:webhook slack) :stop :noop)
(defstate client-id :start (:client-id slack) :stop :noop)
(defstate client-secret :start (:client-secret slack) :stop :noop)
