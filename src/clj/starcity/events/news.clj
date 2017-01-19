(ns starcity.events.news
  (:require [datomic.api :as d]
            [starcity.datomic :refer [conn]]
            [starcity.events.plumbing :refer [defproducer]]
            [starcity.models.news :as news]))

(defproducer dismiss! ::dismiss [news]
  @(d/transact conn [(news/dismiss news)]))
