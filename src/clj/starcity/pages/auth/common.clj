(ns starcity.pages.auth.common
  (:require [bouncer.validators :as v]))

(defn required
  [message]
  [v/required :message message])

(defn errors-from
  "Extract errors from a bouncer error map."
  [[errors _]]
  (reduce (fn [acc [_ es]] (concat acc es)) [] errors))

(defn valid?
  [[errors result]]
  (if (nil? errors)
    result
    false))
