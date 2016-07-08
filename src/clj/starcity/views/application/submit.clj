(ns starcity.views.application.submit
  (:require [starcity.views.application.common :as common]))

(defn submit
  [current-steps & {:keys [errors]}]
  (let [sections []]
    (common/application
     current-steps
     [:div.question-container
      (common/error-alerts errors)
      [:form {:method "POST"}
       [:ul.question-list
        (for [[title content] sections]
          (common/section title content))]
       common/onward]]
     :title "Submit"
     :js [])))
