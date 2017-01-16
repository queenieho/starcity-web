(ns starcity.config.slack
  (:require [starcity.config :refer [config]]
            [starcity.environment :refer [environment]]
            [mount.core :refer [defstate]]))

(defstate slack :start (:slack config) :stop :noop)
(defstate webhook-url :start (:webhook slack) :stop :noop)
(defstate client-id :start (:client-id slack) :stop :noop)
(defstate client-secret :start (:client-secret slack) :stop :noop)

(defstate username
  :start (get {:staging     "staging"
               :production  "production"
               :development "debug"}
              environment))
