(ns admin.accounts.check-form.views
  (:refer-clojure :exclude [val])
  (:require [ant-ui.core :as a]
            [admin.util :as u]
            [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [toolbelt.core :as tb]
            [clojure.string :as string]))

(defn on-change
  ([key]
   (on-change key identity))
  ([key tf]
   #(dispatch [:check-form/update key (tf %)])))

(defn- val [e]
  (.. e -target -value))

(def statuses
  [:check.status/received
   ;; NOTE: After talking with Jesse, the thought is that the `deposited` status
   ;; is unnecessary
   ;; :check.status/deposited
   :check.status/cleared
   :check.status/canceled
   :check.status/bounced])

(def recover-status
  (reduce
   (fn [m s]
     (assoc m (name s) s))
   {}
   statuses))

(defn- footer []
  (let [can-submit (subscribe [:check-form/can-submit?])
        submitting (subscribe [:check-form/submitting?])]
    (fn []
      [:div
       [a/button {:size     :large
                  :on-click #(dispatch [:check-form/hide])}
        "Cancel"]
       [a/button {:size     :large
                  :type     :primary
                  :disabled (not @can-submit)
                  :loading  @submitting
                  :on-click #(dispatch [:check-form/submit])}
        "Save"]])))

(defn modal []
  (let [visible (subscribe [:check-form/showing?])
        data    (subscribe [:check-form/form-data])
        title   (subscribe [:check-form/title])]
    (fn []
      [a/modal {:title     @title
                :visible   @visible
                :footer    (r/as-element [footer])
                :on-cancel #(dispatch [:check-form/hide])}
       ;; TOP ROW
       [:div.field.is-grouped
        [:div.control.is-expanded
         [:label.label "Amount (dollars)"]
         [a/input-number
          {:value       (:amount @data)
           :on-change   (on-change :amount)
           :min         1
           :placeholder "e.g. $500"
           :style       {:width "100%"}}]]
        [:div.control.is-expanded
         [:label.label "Name on check"]
         [a/input {:type      "text"
                   :value     (:name @data)
                   :on-change (on-change :name val)}]]
        [:div.control.is-expanded
         [:label.label "Bank"]
         [a/input {:type        "text"
                   :placeholder "e.g. Wells Fargo"
                   :value       (:bank @data)
                   :on-change   (on-change :bank val)}]]]

       ;; MIDDLE ROW
       [:div.field.is-grouped
        [:div.control.is-expanded
         [:label.label "Check number"]
         [a/input {:placeholder "e.g. 1234"
                   :value       (str (:number @data))
                   :on-change   (on-change :number (comp tb/str->int val))}]]
        [:div.control.is-expanded
         [:label.label "Date on Check"]
         [a/input {:type      "date"
                   :class     "ant-input"
                   :value     (when-let [d (:date @data)]
                                (u/date->input-format d))
                   :on-change (on-change :date (comp u/input-format->date val))}]]
        [:div.control.is-expanded
         [:label.label "Date received"]
         [a/input {:type      "date"
                   :class     "ant-input"
                   :value     (when-let [d (:received-on @data)]
                                (u/date->input-format d))
                   :on-change (on-change :received-on (comp u/input-format->date val))}]]]

       ;; BOTTOM ROW
       [:div.field.is-grouped
        [:div.control.is-expanded
         [:label.label "Status"]
         [a/select {:value       (when-let [s (:status @data)] (name s))
                    :placeholder "Choose status"
                    :style       {:width "100%"}
                    :on-change   (on-change :status recover-status)}
          (map-indexed
           (fn [i s]
             ^{:key i} [a/select-option {:value (name s)} (-> s name string/capitalize)])
           statuses)]]]])))
