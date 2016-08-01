(ns starcity.views.application.submit
  (:require [starcity.views.application.common :as common]
            [starcity.config :refer [config]]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn checkbox
  [& {:keys [label] :as attrs}]
  [:p
   [:input (-> (dissoc attrs :label)
               (assoc :type "checkbox"))]
   [:label {:for (:id attrs)} label]])

(defn validation-group
  [& children]
  [:div.validation-group children])

;; =============================================================================
;; Sections
;; =============================================================================

(def ^:private tos-section
  (validation-group
   (checkbox :label "Yes &mdash; I have read and agree to both."
             :id "tos-acknowledged"
             :name "tos-acknowledged"
             :required true)))

;; =============================================================================
;; Background Check Permission

(def ^:private permission-section
  (let [bg-name "background-permission"
        copy-name "receive-background-check"]
    (validation-group
     (checkbox :id bg-name :name bg-name :required true :label "Yes")
     [:div#receive-copy {:style "display: none;"}
      (checkbox :id copy-name :name copy-name :label "Send me a copy of my background check.")]
     [:p.help-block "To ensure the safety of our community members, we require background checks on all applicants."]
     ;; NOTE: This needs to be outside, otherwise it gets the .has-error styling from jquery-validation/bs

     )))

(def ^:private payment-section
  (list
   [:input#stripe-token {:type "hidden" :name "stripe-token"}]
   [:p.help-block.help-description "The application fee is only to cover the cost of running a background check and verifying your income."]))

;; =============================================================================
;; API
;; =============================================================================

(def ^:private submit-button
  [:button#stripe-btn.btn.waves-effect.waves-light.btn-large
   {:type "Submit"}
   "Pay &amp; Submit"])

(defn submit
  [current-steps email errors]
  (let [sections [[[:span "Have you read our "
                    [:a {:href "/terms" :target "_blank"} "Terms of Service"]
                    " &amp; "
                    [:a {:href "/privacy" :target "_blank"} "Privacy Policy"]
                    "?"]
                   tos-section]
                  ["Do we have your permission to run a background check?"
                   permission-section]
                  ["Finally, please pay the $20 application fee."
                   payment-section]]]
    (common/step "Submit" sections current-steps
                 :submit-button submit-button
                 :errors errors
                 :json [["stripe" {:key   (get-in config [:stripe :public-key])
                                   :email email}]]
                 :js ["https://checkout.stripe.com/checkout.js"])))
