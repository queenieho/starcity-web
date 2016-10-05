(ns starcity.config.stripe
  (:require [starcity.config :refer [config]]
            [mount.core :as mount :refer [defstate]]))

(defstate stripe :start (:stripe config))
(defstate public-key :start (:public-key stripe))
(defstate secret-key :start (:secret-key stripe))
