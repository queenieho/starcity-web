(ns mars.account.rent.components.upcoming
  (:require [re-frame.core :refer [dispatch subscribe]]
            [mars.components.antd :as a]
            [cljs-time.format :as f]
            [starcity.components.icons :as i]
            [starcity.log :as l]))

(def ^:private date-formatter (f/formatter "M/d/yy"))

(defn upcoming [payment loading]
  [a/card {:title   "Your Rent"
           :loading loading
           :class   "upcoming"}
   [:div.columns.is-mobile
    [:div.column.has-text-centered
     [:p.heading "amount"]
     [:p.subtitle.is-5 (->> (:amount payment) int (str "$"))]]
    [:div.column.has-text-centered
     [:p.heading "due by"]
     [:p.subtitle.is-5
      (when-let [due-by (:due-by payment)]
        (f/unparse date-formatter due-by))]]]])
