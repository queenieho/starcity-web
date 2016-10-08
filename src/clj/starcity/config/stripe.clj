(ns starcity.config.stripe
  (:require [starcity.config :refer [config]]
            [mount.core :as mount :refer [defstate]]))

(defstate stripe :start (:stripe config) :stop {})
(defstate public-key :start (:public-key stripe) :stop "")
(defstate secret-key :start (:secret-key stripe) :stop "")
