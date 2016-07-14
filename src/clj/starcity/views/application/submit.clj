(ns starcity.views.application.submit
  (:require [starcity.views.application.common :as common]
            [starcity.config :refer [config]]))

;; =============================================================================
;; Helpers
;; =============================================================================


;; =============================================================================
;; Sections
;; =============================================================================

(defn- tos-section
  []
  (let [id "tos-acknowledged"]
    [:div.form-group
     [:label.control.control--checkbox {:for id}
      [:input
       {:id       id
        :type     "checkbox"
        :name     id
        :required true}
       "Yes &mdash; I have read and agree to both."]
      [:div.control__indicator]]]))

;; =============================================================================
;; Background Check Permission

(defn- permission-section
  []
  [:div
   (let [name "background-permission"]
     [:div.form-group
      [:label.control.control--checkbox {:for name}
       [:input
        {:id       name
         :type     "checkbox"
         :name     name
         :required true}
        "Yes"]
       [:div.control__indicator]]])

   ;; NOTE: This needs to be outside, otherwise it gets the .has-error styling from jquery-validation/bs
   [:p.help-block "To ensure the safety of our community members, we require background checks on all applicants."]

   (let [name "receive-background-check"]
     [:div#receive-copy.form-group {:style "display: none;"}
      [:label.control.control--checkbox {:for name}
       [:input
        {:id   name
         :type "checkbox"
         :name name}
        "Send me a copy of my background check."]
       [:div.control__indicator]]])])

(defn- payment-section
  []
  [:p.help-block.help-description "The application fee is only to cover the cost of running a background check and verifying your income."])

;; =============================================================================
;; API
;; =============================================================================

(defn submit
  [current-steps email errors]
  (let [sections [[[:span "Have you read our "
                    [:a {:href "/tos" :target "_blank"} "Terms of Service"]
                    " &amp; "
                    [:a {:href "/privacy" :target "_blank"} "Privacy Policy"]
                    "?"]
                   (tos-section)]
                  ["Do we have your permission to run a background check?"
                   (permission-section)]
                  ["Finally, please pay the $20 application fee."
                   (payment-section)]]]
    (common/application
     current-steps
     [:div.question-container
      (common/error-alerts errors)
      [:form {:method "POST" :action ""}
       [:input#stripe-token {:type "hidden" :name "stripe-token"}]
       [:ul.question-list
        (for [[title content] sections]
          (common/section title content))]
       (common/submit-button "Pay &amp; Submit" "stripe-btn")]]
     :title "Submit"
     :json [["stripe" {:key   (get-in config [:stripe :public-key])
                       :email email}]]
     :js ["https://checkout.stripe.com/checkout.js"
          "bower/jquery-validation/dist/jquery.validate.js"
          "validation-defaults.js"
          "submit.js"])))
