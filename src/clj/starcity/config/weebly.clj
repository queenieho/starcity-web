(ns starcity.config.weebly
  (:require [starcity.config :refer [config]]
            [mount.core :refer [defstate]]))

(defstate weebly :start (:weebly config))
(defstate site-id :start (:site-id weebly))
(defstate form-id :start (:form-id weebly))
