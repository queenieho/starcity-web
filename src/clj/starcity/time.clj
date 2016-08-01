(ns starcity.time
  (:require [clj-time
             [coerce :as c]
             [format :as f]
             [periodic :as p]
             [core :as t]]))

(defn first-day-of-month []
  (let [now (t/now)]
    (t/date-time (t/year now) (t/month now))))

(defn next-twelve-months []
  (take 12 (p/periodic-seq (first-day-of-month) (t/months 1))))
