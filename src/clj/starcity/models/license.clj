(ns starcity.models.license
  (:require [starcity.datomic :refer [conn]]
            [starcity.models.util :refer :all]
            [datomic.api :as d]))

(defn licenses []
  (->> (find-all-by (d/db conn) :license/term)
       (entids)
       (d/pull-many (d/db conn) [:db/id :license/term])))
