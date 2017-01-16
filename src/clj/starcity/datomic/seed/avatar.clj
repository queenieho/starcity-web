(ns starcity.datomic.seed.avatar
  (:require [datomic.api :as d]
            [starcity.datomic.partition :refer [tempid]]))

(defn seed [conn]
  @(d/transact conn [{:db/id       (tempid)
                      :avatar/name :system
                      :avatar/url  "/assets/img/starcity-logo-black.png"}]))
