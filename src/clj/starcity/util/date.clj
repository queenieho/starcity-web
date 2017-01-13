(ns starcity.util.date
  (:require [clj-time.core :as t]
            [clj-time.coerce :as c]))

(defn is-first-day-of-month? [dt]
  (= (t/day dt) 1))

(defn end-of-day
  "Produce a date that is on the same day as `date`, but with time set to the
  last second in `date`."
  [date]
  (let [date (c/to-date-time date)]
    (-> (t/date-time (t/year date) (t/month date) (t/day date))
        (t/plus (t/days 1))
        (t/minus (t/seconds 1))
        (c/to-date))))

(defn beginning-of-day
  "Produce a date that is on the same day as `date`, but with time set to the
  first second in `date`."
  [date]
  (-> (c/to-date-time date)
      (t/floor t/day)
      (c/to-date)))
