(ns starcity.views.onboarding
  (:refer-clojure :exclude [next])
  (:require [hiccup
             [def :refer [defelem]]
             [form :as f]]
            [starcity.countries :refer [countries]]
            [starcity.views.components
             [form :refer [control label]]
             [layout :as l]]
            [starcity.views.page :as p]))

;; =============================================================================
;; Internal
;; =============================================================================

(defn- wrapper [content]
  (fn [req]
    (l/section
     {:style "flex-grow: 1"}
     (l/container
      (p/messages req)
      (content req)))))

(defn- prompt [& content]
  [:div.prompt content])

(defn- form [& content]
  (prompt
   [:form {:method "POST"}
    content]))

(defn- title [& content]
  [:header
   [:h3.title.is-3 content]])

(defn- body [& content]
  [:div.prompt-content.content.is-medium {:style "min-height: 300px;"}
   content])

(defelem next
  ([]
   (next "Next" false))
  ([text]
   (next text false))
  ([text disabled]
   [:button.button.is-medium.is-primary.is-pulled-right.submit
    {:type "submit" :disabled (when disabled true) :class (when disabled "is-disabled")}
    (or text "Next")
    [:span.icon [:i.fa.fa-angle-right]]]))

(defn- back [to]
  [:a.button.is-medium.is-pulled-left {:href to}
   [:span.icon [:i.fa.fa-angle-left]]
   "Back"])

;; =============================================================================
;; Content

(defn- begin-content [_]
  (prompt
   (title "Congratulations! You're almost a Starcity Member!")
   (body
    [:p "Now that you've been approved for the community, please pay your security deposit to reserve your room."]
    [:p "Your room will be held for <strong>one week</strong> from the date that
    you were approved."])

   (control
    [:a.button.is-primary.is-medium.is-pulled-right
     {:href "/onboarding/security-deposit/payment-method"}
     "Begin"])))

(defn- payment-method-content [{method :payment-method}]
  (let [is-checked? (fn [m] (and method (= m (name method))))]
    (form
     (title "How would you like to pay your security deposit?")
     (body
      [:p "There are two options available:"]
      [:ul
       [:li "You can pay us <strong>electronically</strong> by using an ACH transfer from your bank account, or"]
       [:li "You can mail us a <strong>check</strong>."]]
      [:div.form-container
       (label "payment-method" "Choose an option here.")
       (control
        [:label.radio
         [:input {:type     "radio"
                  :name     "payment-method"
                  :value    "ach"
                  :required true
                  :checked  (is-checked? "ach")}]
         "Electronically using ACH"])
       (control
        [:label.radio
         [:input {:type     "radio"
                  :name     "payment-method"
                  :value    "check"
                  :required true
                  :checked  (is-checked? "check")}]
         "Via mail with a check"])])
     (control (next) (back "/onboarding")))))

(def ^:private entity-names
  {"52gilbert"   "52 Gilbert LLC"
   "2072mission" "2072-2074 Mission LLC"})

(defn- pay-by-check-content [{rent :monthly-rent, property-code :property-code}]
  (form
   (title "Here's the information you'll need to write your check.")
   (body
    [:p "There are two payment options available:"]
    [:ul
     [:li (format "You can pay either <b>$500 now</b> and the remainder ($%s) by the end of the first month, or"
                  (int (- rent 500)))]
     [:li (format "The full amount of <b>$%s</b> now." (int rent))]]
    [:p "You'll be able to advance beyond this step after we have received your
    check and have verified that it clears."]
    [:div.form-container
     [:p
      "Please make your check payable to: "
      [:strong (get entity-names property-code)]]
     [:p
      "And please mail your check to: "
      [:strong "Starcity Properties Inc., 995 Market St. Floor 2, San Francisco CA, 94103"]]])
   (control
    (back "/onboarding/security-deposit/payment-method"))))

;; TODO: Model layer?
(def ^:private currencies
  ["USD" "AUD" "CAD" "EUR" "GBP" "DKK" "NOK" "SEK" "JPY" "SGD"])

(defn- enter-bank-info-content [_]
  (form
   (title "In order to pay, you'll first need to verify ownership of your bank account.")
   (body
    [:p "Please enter your bank account information below."]
    [:p "After entering your account information, we'll make <b>two small
    deposits</b> (under $1) to your account some time in the next couple of
    business days."]
    [:p "In the next step, you'll verify ownership by correctly submitting the
    amounts of the deposits that were made."]
    [:div.form-container
     (f/hidden-field "stripe-token")
     (l/columns
      {:class "control"}
      (l/column
       {:class "control is-half"}
       (label "account-holder-name" "Account Holder's Name")
       (f/text-field {:class "input" :required true} "account-holder-name"))
      (l/column
       {:class "control"}
       (label "country" "Country")
       [:span.select.is-fullwidth
        (f/drop-down {:id "country"} "country"
                     (map (fn [{:keys [name code]}] [name code]) countries)
                     "US")])
      (l/column
       {:class "control"}
       (label "currency" "Currency")
       [:span.select.is-fullwidth
        (f/drop-down "currency"
                     currencies
                     "USD")]))

     ;; Bottom Row
     (l/columns
      {:class "control"}
      (l/column
       {:class "control"}
       (label "routing-number" "Routing Number")
       (f/text-field {:class "input" :required true} "routing-number"))
      (l/column
       {:class "control"}
       (label "account-number" "Account Number")
       (f/text-field {:class "input" :required true} "account-number")))]
    (control
     (back "/onboarding/security-deposit/payment-method")
     (next)))))

(defn- verify-microdeposits-content [_]
  (form
   (title "Great! We've initiated the verification process.")
   (body
    [:p "Over the next <b>24-48 hours</b>, two small deposits will be made in
         your account with the statement description <b>VERIFICATION</b> &mdash;
         enter them below to verify that you are the owner of this bank account."]
    (let [attrs {:min 1 :step 1}]
      [:div.form-container
       (l/columns
        {:class "control"}
        (l/column
         {:class "control"}
         (label "deposit-1" "First deposit amount (cents)")
         [:input.input (merge {:name        "deposit-1"
                               :required    true
                               :type        "number"
                               :placeholder "e.g. 32"} attrs)])
        (l/column
         {:class "control"}
         (label "deposit-2" "Second deposit amount (cents)")
         [:input.input (merge {:name        "deposit-2"
                               :required    true
                               :type        "number"
                               :placeholder "e.g. 45"} attrs)]))])
    (control
     (next)))))

(defn- confirmation-modal [property-code]
  "The confirmation modal that shows compliance-related information as per:
  https://support.stripe.com/questions/accepting-ach-payments-with-stripe#ach-authorization"
  [:div#confirmation-modal.modal
   [:div.modal-background]
   [:div.modal-container
    [:div.modal-content
     [:div.box
      [:div.content.is-medium
       [:p.title.is-3 "Payment Confirmation"]
       [:p (format "By pressing the <strong>Pay Now</strong> button below I
                    authorize <strong>%s</strong> to electronically debit my
                    account and, if necessary, electronically credit my account
                    to correct erroneous debits."
                   (get entity-names property-code))]]

      [:button#submit.button.is-success.is-medium {:type "button"} "Pay Now"]]]]
   [:button.modal-close {:type "button"}]])

;; And here's the complete view.

(defn- pay-by-ach-content [{rent :monthly-rent, code :property-code :as req}]
  [:div
   (confirmation-modal code)
   (form
    (title "Your bank account has been verified!")
    (body
     [:p "We can now accept your payment. There are two options:"]
     [:ul
      [:li (format "You can pay either <b>$500 now</b> and the remainder ($%s) by the end of the first month, or"
                   (int (- rent 500)))]
      [:li (format "The full amount of <b>$%s</b> now." (int rent))]]
     [:div.form-container
      (label "payment-choice" "Choose the amount that you would like to pay.")
      (control
       [:label.radio
        (f/radio-button {:required true} "payment-choice" false "full")
        (format "Full amount ($%s)" (int rent))])
      (control
       [:label.radio
        (f/radio-button {:required true} "payment-choice" false "partial")
        "Partial amount ($500 now, rest later)"])]))
   (control
    [:button#pay-btn.button.is-medium.is-primary.is-pulled-right
     {:type "button" :disabled true :class "is-disabled"}
     "Pay"
     [:span.icon [:i.fa.fa-angle-right]]])])

;; =============================================================================
;; API
;; =============================================================================

(defn- page
  "Construct an onboarding page."
  [title primary-content & content]
  (apply p/page
         (p/title title)
         p/navbar
         (wrapper primary-content)
         content))

(def begin
  (page "Onboarding" begin-content))

(def payment-method
  (page "Choose Payment Method" payment-method-content))

(def pay-by-check
  (page "Pay by Check" pay-by-check-content))

(defn enter-bank-information [stripe-public-key]
  (page "Enter Bank Account Information"
        enter-bank-info-content
        (p/json ["stripe" {:key stripe-public-key}])
        (p/scripts "https://js.stripe.com/v2/"
                   "/assets/bower/jquery-validation/dist/jquery.validate.min.js"
                   "/js/bank-info.js")))

(def verify-microdeposits
  (page "Verify Bank Account" verify-microdeposits-content))

(def pay-by-ach
  (page "Make ACH Payment" pay-by-ach-content (p/scripts "/js/pay-by-ach.js")))

(def security-deposit-complete
  (p/page
   (p/title "Security Deposit Paid")
   p/navbar
   (fn [{property-name :property-name}]
     (l/section
      {:class "is-fullheight"}
      (l/container
       [:h1.title.is-1 "Your payment is complete."]
       [:p.subtitle.is-3
        {:style "margin-top: 30px;"}
        (format "We've received and are processing your payment, and have
                reserved your room at <b>%s</b>." property-name)]
       [:p.subtitle.is-3
        "Expect us to be in touch soon to coordinate your move-in."])))))
