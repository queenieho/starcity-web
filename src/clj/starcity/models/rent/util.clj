(ns starcity.models.rent.util
  (:require [clj-time.core :as t]))

(defn first-day-next-month [date-time]
  (t/plus (t/first-day-of-the-month date-time)
          (t/months 1)))
