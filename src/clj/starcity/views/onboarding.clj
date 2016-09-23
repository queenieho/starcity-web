(ns starcity.views.onboarding
  (:require [starcity.views.components :refer :all]
            [starcity.views.common :as common :refer [defpage]]))

;; =============================================================================
;; Internal
;; =============================================================================

(defn- container
  [title req & content]
  (let [title-bar [:nav
                   [:div.nav-wrapper.bone-pink.darken-2
                    [:h4.nav-title.truncate title]]]]
    [:main#central-card
     [:div.container
      [:div.row {:style "margin-bottom: 0;"}
       [:div.col.s12.m12.l10.offset-l1
        (common/errors req)]]
      [:div.row.section
       [:div.col.s12.m12.l10.offset-l1.card-panel.grey-text.text-darken-2
        title-bar
        content]]]]))

(defn- section
  [title description content & {:keys [back-url has-submit? next]
                                :or   {has-submit? true}}]
  [:div.section
   [:div.row
    [:div.col.s10.offset-s1
     [:h5.section-title title]
     [:div.help-text description]
     (when content
       [:div.section-content
        content])
     [:div.row {:style "margin-top: 30px; margin-bottom: 10px;"}
      [:div.col.s12
       (when back-url
         [:a.btn.waves-effect.waves-light.tranquil-blue
          {:href back-url}
          "Back"])
       (when has-submit?
         (if next
           next
           [:button.right.btn.waves-effect.waves-light.star-green.lighten-1 {:type "submit"}
            "Next"]))]]]]])

;; =============================================================================
;; API
;; =============================================================================

(defpage begin
  (title "Welcome!")
  (body
   (container
    "Welcome to Starcity!"
    req
    [:div.section
     [:div.row
      [:div.col.s10.offset-s1
       [:p.flow-text "Now that you've been approved for the community, please pay your security deposit to reserve your room."]
       [:p.flow-text
        "Your room will be held for "
        [:strong "one week"]
        " from the date that you were approved."]
       [:div.row {:style "margin-top: 30px; margin-bottom: 10px;"}
        [:div.col.s12
         [:a.btn.right.waves-effect.waves-light.star-green.lighten-1
          {:href "/onboarding/security-deposit/payment-method"}
          "Begin"]]]]]])))

(defpage choose-payment-method
  [req method]
  (title "Choose Payment Method")
  (body
   (let [is-selected? (fn [m] (and method (= m (name method))))]
     (container
      "Security Deposit: Choose Payment Method"
      req
      [:form {:action "/onboarding/security-deposit/payment-method" :method "POST"}
       (section "How would you like to pay?"
                ""
                [:div.validation-group
                 (radio-group "payment-method"
                              ["ach" "Electronic payment using your bank (fastest)" {:required true :selected (is-selected? "ach")}]
                              ["check" "Mail a check" {:selected (is-selected? "check")}])]
                :back-url "/onboarding")]))))


(defpage pay-by-check [req full-payment-amount]
  (title "Pay By Check")
  (body
   (container
    "Security Deposit: Pay by Check"
    req
    (section "You can write your check using the information provided below."
             "We will confirm receipt of your payment after we receive your check."
             [:div
              [:p "You can pay either " [:b "$500 now"] " (with the remainder due by the end of the first month),"
               [:i " OR "] "the full security deposit of " [:b (str "$" (int full-payment-amount))] "."]
              [:p "Make check payable to: " [:b "Starcity Properties, Inc."]]
              [:p "Mail check to: " [:b "Starcity Properties, Inc., 995 Market St. Floor 2, San Francisco CA, 94103"]]]
             :has-submit? false
             :back-url "/onboarding/security-deposit/payment-method"))))

;; =====================================
;; Verify Bank Account

(defpage enter-bank-information [req stripe-public-key]
  (title "Verify Bank Account")
  (body
   (container
    "Security Deposit: Pay Electronically"
    req
    [:form
     {:action "/onboarding/security-deposit/payment-method/ach/verify" :method "POST"}
     [:input#stripe-token {:type "hidden" :name "stripe-token"}]
     (section "In order to pay, you'll first need to verify your bank account."
              ""
              [:div
               ;; Account holder name
               [:div.row
                [:div.col.s12
                 (input-field :name "account-holder-name"
                              :required true
                              :label "Account Holder Name")]]

               ;; TODO: Optional if currency is EUR
               ;; routing number
               [:div.row
                [:div.col.s12
                 (input-field :name "routing-number"
                              :required true
                              :label "Routing Number")]]

               ;; account number
               [:div.row
                [:div.col.s12
                 (input-field :name "account-number"
                              :required true
                              :label "Account Number")]]

               ;; individual or company account?
               [:div.row
                [:div.col.s12
                 (select-field :name "account-holder-type"
                               :choose-msg "Choose account type"
                               :label "Account Type"
                               :selected "individual"
                               :required true
                               :options [["individual" "Individual"]
                                         ["company" "Company"]])]]

               ;; country, currency selects
               ;; [:div.row
               ;;  [:div.col.s6
               ;;   (select-field :name "country"
               ;;                 :choose-msg "Choose country"
               ;;                 :label "Country"
               ;;                 :selected "US"
               ;;                 :required true
               ;;                 :options [["US" "US"]])] ; TODO: Add more countries
               ;;  [:div.col.s6
               ;;   (select-field :name "currency"
               ;;                 :choose-msg "Choose currency"
               ;;                 :label "Currency"
               ;;                 :selected "USD"
               ;;                 :required true
               ;;                 :options [["USD" "US Dollar"]])]]
               ]
              :back-url "/onboarding/security-deposit/payment-method")]))
  (json ["stripe" {:key stripe-public-key}])
  (js "https://js.stripe.com/v2/"))

(defpage verify-microdeposits [req]
  (title "Finish Verification")
  (body
   (container
    "Security Deposit: Verify Bank Account"
    req
    [:form
     {:action "/onboarding/security-deposit/payment-method/ach/microdeposits" :method "POST"}
     (section "Great! We've initiated the verification process."
              "Over the next <b>24-48 hours</b>, two small deposits will be made in
              your account with statement description <b>VERIFICATION</b>
              &mdash; enter them below to verify that you are the owner of this
              bank account."
              (let [attrs {:min 1 :step 1}]
                [:div.row
                 [:div.col.s6
                  (input-field :name "deposit-1"
                               :required true
                               :type "number"
                               :label "First deposit (cents)"
                               :placeholder "e.g. 32"
                               :attrs attrs)]
                 [:div.col.s6
                  (input-field :name "deposit-2"
                               :required true
                               :type "number"
                               :label "Second deposit (cents)"
                               :placeholder "e.g. 45"
                               :attrs attrs)]]))])))

;; =====================================
;; Pay by ACH

;; This is the final portion of the security deposit by ACH flow, of which
;; there's a lot.

;; The idea is to choose between paying the full deposit or a part-now
;; part-later scenario, and then to see a final confirmation modal that
;; does all the compliance stuff.

;; Here's said modal:

(def ^:private confirmation-modal
  "The confirmation modal that shows compliance-related information as per:
  https://support.stripe.com/questions/accepting-ach-payments-with-stripe#ach-authorization"
  [:div#confirmation-modal.modal
   [:div.modal-content
    [:h4 "Payment Confirmation"]

    [:p.flow-text-small "By pressing the "
     [:b "Pay Now"]
     " button below I authorize Starcity Properties, Inc. to
     electronically debit my account and, if necessary, electronically credit my
     account to correct erroneous debits."]]
   [:div.modal-footer
    [:button#submit.modal-action.waves-effect.waves-star-green.btn-flat
     {:type "button"}
     "Pay Now"]
    [:a.modal-action.modal-close.waves-effect.waves-grey.btn-flat {:href "#"}
     "Cancel"]]])

;; And here's the complete view.

(defpage pay-by-ach [req full-payment-amount]
  (title "Pay Security Deposit")
  (body
   (container
    "Security Deposit: Complete Payment"
    req
    [:form
     {:method "POST"
      :action "/onboarding/security-deposit/payment-method/ach/pay"}
     (section "Hooray! Your bank account has been verified &mdash; now we can accept your payment."
              "There are two options:"
              [:div.validation-group
               (radio-group "payment-choice"
                            ["full" (str "Pay full amount ($" (int full-payment-amount) ") now")   {:required true}]
                            ["partial" "Pay $500 now, and the rest at after your first month"])]
              :next [:button#pay-btn.right.btn.waves-effect.waves-light.star-green.lighten-1.disabled
                     {:type "button" :disabled true}
                     "Pay"])]
    confirmation-modal)))

;; =====================================
;; Security deposit complete

;; TODO: Get property name
(defpage security-deposit-complete
  [req property-name]
  (title "Onboarding Complete")
  (body
   (container
    "Security Deposit: Sucessfully Paid"
    req
    (section (format "Thank you! We have reserved your room at %s." property-name)
             "We'll be in touch soon to coordinate your move-in."
             nil
             :has-submit? false))))
