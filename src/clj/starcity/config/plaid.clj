(ns starcity.config.plaid
  (:require [starcity.config :refer [config]]
            [mount.core :refer [defstate]]))

(defstate plaid :start (:plaid config))
(defstate env :start (:env plaid))
(defstate client-id :start (:client-id plaid))
(defstate secret :start (:secret plaid))
(defstate public-key :start (:public-key plaid))
