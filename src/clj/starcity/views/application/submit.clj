(ns starcity.views.application.submit
  (:require [starcity.views.application.common :as common]
            [starcity.views.components :refer [checkbox validation-group]]
            [starcity.config :refer [config]]))

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
  (let [bg-name   "background-permission"
        copy-name "receive-background-check"]
    (validation-group
     (checkbox :id bg-name
               :name bg-name
               :required true
               :label "Yes &mdash; I authorize Starcity to perform a background check on me.")
     [:div#receive-copy {:style "display: none;"}
      [:p
       [:input {:type "checkbox" :id copy-name :name copy-name}]
       [:label {:for copy-name}
        "Please send me a copy of my background check."]
       [:small {:style "display: block; padding-left: 35px;"}
        "By checking this box, I am requesting a free copy of my safety check as permitted by California law."]]]
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
       [:a#background-check-agree.modal-action.modal-close.waves-effect.waves-green.btn-flat {:href "#"}
        "Agree"]
       [:a.modal-action.modal-close.waves-effect.waves-red.btn-flat {:href "#"}
        "Disagree"]]]
     )))
;; =============================================================================
;; Plaid

(def ^:private file-upload
  [:div.col.m5.s12.center
   [:div.row
    [:div.col.s12.file-field.input-field
     [:div.btn.tranquil-blue.darken-1
      [:i.large.material-icons  "vertical_align_top"]
      [:input {:type "file" :multiple true :name "income-files"}]]
     [:div.file-path-wrapper
      [:input.file-path.validate {:type "text" :placeholder "Upload one or more files"}]]]]
   [:div.row
    [:div.col.s12
     [:p.center.light "Pay-stubs or other proof of income are acceptable."]]]])

(def ^:private bank-account-info-modal
  [:div#bank-account-info-modal.modal
   [:div.modal-content
    [:h3 "What is this?"]
    [:p.flow-text-small "Credit checks are a pain, and can negatively affect your credit score. This is "
     [:b "not a credit check"] "!"]
    [:p.flow-text-small "Instead, Starcity uses a service called "
     [:a {:href "https://plaid.com/products/income" :target "_blank"} "Plaid"]
     " to receive income information directly from your bank account. We only use this information to determine your ability to pay rent &mdash; "
     [:i "we do not use this information for any other purpose."]]]
   [:div.modal-footer
    [:a.modal-action.modal-close.waves-effect.waves-star-green.btn-flat {:href "#!"} "Got It"]]])

(defn- use-bank-account [plaid-id]
  [:div.col.m5.s12.center
   [:div.row
    [:div.col.s12
     (if plaid-id
       [:button#link-button.btn.disabled {:type "button"}
        [:i.material-icons.right "done"] "Verified"]
       [:button#link-button.btn.star-green {:type "button"}
        "Use Bank Account"])]]
   [:div.row
    [:div.col.s12
     [:p.center.light
      "Give us a quick financial snapshot from your bank."]
     [:a.modal-trigger.waves-effect.waves-teal.btn-flat.tranquil-blue-text.light {:href "#!"}
      "Learn More"
      [:i.material-icons.right "open_in_new"]]]]])

(defn- income-section
  [plaid-id]
  [:div#income-section
   [:div.row
    file-upload
    ;; divider on mobile
    [:div.col.l2.hide-on-small-only
     [:p.center [:b "OR"]]]
    [:div.hide-on-med-and-up.col.s12 {:style "margin-bottom: 20px; margin-top: 10px;"}
     [:p.center [:b "OR"]]]
    (use-bank-account plaid-id)]
   bank-account-info-modal])

;; =============================================================================
;; Payment

(def ^:private payment-section
  (list
   [:input#stripe-token {:type "hidden" :name "stripe-token"}]
   [:p.help-block.help-description "The application fee is only to cover the cost of running a background check and verifying your income."]))

;; =============================================================================
;; API
;; =============================================================================

(def ^:private submit-button
  [:div#submit-section
   [:button#stripe-btn.btn.waves-effect.waves-light.btn-large
    {:type "Submit"}
    "Pay &amp; Submit"]
   [:div#loading {:style "display: none;"}
    [:p.center.flow-text "Submitting..."]
    [:div.preloader-wrapper.big.active
     [:div.spinner-layer.spinner-blue-only
      [:div.circle-clipper.left
       [:div.circle]]
      [:div.gap-patch
       [:div.circle]]
      [:div.circle-clipper.right
       [:div.circle]]]]]])

(defn submit
  [req current-steps email plaid-id amount errors]
  (let [sections (map (partial apply common/make-step)
                      [[[:span "Have you read our "
                         [:a {:href "/terms" :target "_blank"} "Terms of Service"]
                         " &amp; "
                         [:a {:href "/privacy" :target "_blank"} "Privacy Policy"]
                         "?"]
                        tos-section]
                       ["Do we have your permission to run a background check?"
                        [:p.light
                         "To ensure the safety of our community members, we require background checks on all applicants."]
                        permission-section]
                       ["Almost done! Please verify your ability to pay rent."
                        [:p.light "Choose" [:b " one "] "of the following:"]
                        (income-section plaid-id)]
                       [(format "Finally, please pay the $%s application fee." (/ amount 100))
                        payment-section]])]
    (common/step req "Submit" sections current-steps
                 :encoding-type "multipart/form-data"
                 :submit-button submit-button
                 :errors errors
                 :json [["stripe" {:key    (get-in config [:stripe :public-key])
                                   :email  email
                                   :amount amount}]
                        ["plaid" {:key      (get-in config [:plaid :public-key])
                                  :env      (get-in config [:plaid :env])
                                  :complete (not (nil? plaid-id))}]]

                 :js ["https://cdn.plaid.com/link/stable/link-initialize.js"
                      "https://checkout.stripe.com/checkout.js"])))
