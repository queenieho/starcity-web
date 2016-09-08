(ns starcity.views.onboarding
  (:require [starcity.views.components :refer :all]
            [starcity.views.common :as common :refer [defpage]]
            [starcity.config :refer [config]]))

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
       (common/errors req)]
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
    [:h1 "Welcome to Gilbert Street!"]

    [:p.flow-text-small "We're really excited to have you on board!"]
    [:p.flow-text-small "In order to hold your room, we need to get a few things out of the way."]

    [:a.btn.btn-large.waves-effect.waves-light.green {:href "/onboarding/security-deposit"}
     "Get Started"])))

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
                "Phasellus neque orci, porta a, aliquet quis, semper a, massa.  Cras placerat accumsan nulla."
                [:div.validation-group
                 (radio-group "payment-method"
                              ["ach" "Pay now via ACH" {:required true :selected (is-selected? "ach")}]
                              ["check" "Send a check" {:selected (is-selected? "check")}])]
                :back-url "/onboarding")]))))


(defpage pay-by-check [req]
  (title "Pay By Check")
  (body
   (container
    "Security Deposit: Pay by Check"
    req
    (section "You can write your check using the information provided below."
             "After we receive your check we'll be in touch about what to do next."
             [:p.flow-text-small "TODO: Check info."]
             :has-submit? false
             :back-url "/onboarding/security-deposit/payment-method"))))

;; =====================================
;; Verify Bank Account

(defpage enter-bank-information [req]
  (title "Verify Bank Account")
  (body
   (container
    "Security Deposit: Pay by ACH"
    req
    [:form
     {:action "/onboarding/security-deposit/payment-method/ach/verify" :method "POST"}
     [:input#stripe-token {:type "hidden" :name "stripe-token"}]
     (section "In order to accept your payment you'll need to verify your bank account."
              "Please enter your bank account information below."
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
                               :required true
                               :options [["individual" "Individual"]
                                         ["company" "Company"]])]]

               ;; country, currency selects
               [:div.row
                [:div.col.s6
                 (select-field :name "country"
                               :choose-msg "Choose country"
                               :label "Country"
                               :selected "US"
                               :required true
                               :options [["US" "US"]])] ; TODO: Add more countries
                [:div.col.s6
                 (select-field :name "currency"
                               :choose-msg "Choose currency"
                               :label "Currency"
                               :selected "USD"
                               :required true
                               :options [["USD" "US Dollar"]])]]]
              :back-url "/onboarding/security-deposit/payment-method")]))
  (json ["stripe" {:key (get-in config [:stripe :public-key])}])
  (js "https://js.stripe.com/v2/"))

(defpage verify-microdeposits [req]
  (title "Finish Verification")
  (body
   (container
    "Security Deposit: Pay by ACH"
    req
    [:form
     {:action "/onboarding/security-deposit/payment-method/ach/microdeposits"
      :method "POST"}
     (section "Enter the microdeposit amounts below to verify your account."
              "Please note that it will take up to two business days before these deposits will be made."
              (let [attrs {:min 1 :step 1}]
                [:div.row
                 [:div.col.s6
                  (input-field :name "deposit-1"
                               :required true
                               :type "number"
                               :label "First deposit"
                               :attrs attrs)]
                 [:div.col.s6
                  (input-field :name "deposit-2"
                               :required true
                               :type "number"
                               :label "Second deposit"
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
     " button below I authorize Starcity Properties Inc. to
     electronically debit my account and, if necessary, electronically credit my
     account to correct erroneous debits."]]
   [:div.modal-footer
    [:button#submit.modal-action.waves-effect.waves-star-green.btn-flat
     {:type "button"}
     "Pay Now"]
    [:a.modal-action.modal-close.waves-effect.waves-grey.btn-flat {:href "#"}
     "Cancel"]]])

;; And here's the complete view.

(defpage pay-by-ach [req]
  (title "Pay Security Deposit")
  (body
   (container
    "Security Deposit: Make Payment"
    req
    [:form
     {:method "POST"
      :action "/onboarding/security-deposit/payment-method/ach/pay"}
     (section "Hooray! Your bank account has been verified &mdash; now we can accept your payment."
              "There are two options:"
              [:div.validation-group
               (radio-group "payment-choice"
                            ["full" "Pay full amount ($1000) now" {:required true}]
                            ["partial" "Pay $500 now, and the rest at move-in"])]
              :next [:button#pay-btn.right.btn.waves-effect.waves-light.star-green.lighten-1.disabled
                     {:type "button" :disabled true}
                     "Pay"])]
    confirmation-modal)))

;; =====================================
;; Security deposit complete

(defpage security-deposit-complete
  (title "Onboarding Complete")
  (body
   (container
    "Security Deposit: Paid"
    req
    (section "You're all set!"
             "We'll be in touch soon with further instructions."
             nil
             :has-submit? false))))
