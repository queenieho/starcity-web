(ns starcity.config.stripe
  (:require [starcity.config :refer [config]]))

(defn stripe [] (:stripe config))
(defn public-key [] (:public-key (stripe)))
(defn secret-key [] (:secret-key (stripe)))
