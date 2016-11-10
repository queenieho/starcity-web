(ns starcity.api.admin.common
  (:require [clojure.spec :as s]))

(s/def ::direction #{:asc :desc})

(defn order-by
  "Order `coll` by `direction`, which is one of `:asc` (ascending)
  or `:desc` (descending)."
  [direction coll]
  (if (#{:asc} direction)
    (reverse coll)
    coll))

(s/fdef order-by
        :args (s/cat :direction ::direction
                     :coll sequential?)
        :ret sequential?)
