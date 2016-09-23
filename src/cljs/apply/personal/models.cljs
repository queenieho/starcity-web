(ns apply.personal.models
  (:require [bouncer.core :as b]
            [bouncer.validators :as v]))

(defn background-complete? [background-info]
  (b/valid?
   background-info
   {:dob     v/required
    :name    {:first v/required
              :last  v/required}
    :address {:city  v/required
              :state v/required
              :zip   [v/required [v/matches #"^\d{5}(-\d{4})?$"]]}
    :consent [v/required true?]}))

(defn phone-number-complete? [phone-number]
  (re-matches #"(^1\d{10}$)|(^[^1]\d{9})" phone-number))
