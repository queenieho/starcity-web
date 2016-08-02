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
      [:p
       [:input {:type "checkbox" :id copy-name :name copy-name}]
       [:label {:for copy-name}
        "Please send me a copy of my background check."]
       [:small {:style "display: block; padding-left: 35px;"}
        "By checking this box, I am requesting a free copy of my safety check as permitted by California law."]]]
     [:p.help-block "To ensure the safety of our community members, we require background checks on all applicants."]
     ;; NOTE: This needs to be outside, otherwise it gets the .has-error styling from jquery-validation/bs
     [:div#background-check-modal.modal.modal-fixed-footer
      [:div.modal-content
       [:h4 "Community Safety Check"]
       [:p "By checking this box, you are providing Starcity written instructions to procure a safety check on you to determine your eligibility for membership and rental capabilities in our community. This safety check is considered an investigative consumer report under California law, and will include a search for criminal records that may be linked to you. All safety checks will be produced in accordance with the Fair Credit Reporting Act and applicable state laws."]
       [:p "You have the right to inquire into whether or not a consumer report was in fact run on you, and, if so, to request the contact information of the consumer reporting agency who furnished the consumer report. You also have the right to view the information that a Consumer Reporting Agency holds on you. You may obtain a copy of this information in person at the Consumer Reporting Agency during regular business hours after providing proper identification and providing reasonable notice for your request. You are allowed to have one additional person accompany you so long as they provide proper identification. Additionally, you can make the same request via the contact information below. The Consumer Reporting Agency can assist you in understanding your file, including coded information."]
       [:ul
        [:li [:b "Community Safety"]]
        [:li [:b "GoodHire, LLC"]]
        [:li "P.O. Box 391146"]
        [:li "Omaha, NE 68139"]
        [:li [:u "Phone:"] " 855-278-7451"]
        [:li [:u "Email:"] [:a {:href "mailto:support@communitysafety.goodhire.com"} " support@communitysafety.goodhire.com"]]]]
      [:div.modal-footer
       [:a.modal-action.modal-close.waves-effect.waves-red.btn-flat {:href "#"}
        "Disagree"]
       [:a#background-check-agree.modal-action.modal-close.waves-effect.waves-green.btn-flat {:href "#"}
        "Agree"]]]
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
  [current-steps email amount errors]
  (let [sections [[[:span "Have you read our "
                    [:a {:href "/terms" :target "_blank"} "Terms of Service"]
                    " &amp; "
                    [:a {:href "/privacy" :target "_blank"} "Privacy Policy"]
                    "?"]
                   tos-section]
                  ["Do we have your permission to run a background check?"
                   permission-section]
                  [(format "Finally, please pay the $%s application fee." (/ amount 100))
                   payment-section]]]
    (common/step "Submit" sections current-steps
                 :submit-button submit-button
                 :errors errors
                 :json [["stripe" {:key     (get-in config [:stripe :public-key])
                                   :email   email
                                   :amount amount}]]
                 :js ["https://checkout.stripe.com/checkout.js"])))
