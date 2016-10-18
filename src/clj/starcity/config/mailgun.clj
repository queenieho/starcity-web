(ns starcity.config.mailgun
  (:require [starcity.config :refer [config]]
            [mount.core :refer [defstate]]))

(defstate mailgun :start (:mailgun config))
(defstate api-key :start (:api-key mailgun))
(defstate domain :start (:domain mailgun))
(defstate default-sender :start (:sender mailgun))
