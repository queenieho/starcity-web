(ns starcity.views.auth.signup
  (:require [starcity.views.base :refer [base]]
            [clojure.string :as s]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- signup-content
  [email first-name last-name errors]
  [:main#signup.auth
   [:div.container
    [:div.row
     [:div.col.l6.offset-l3.m8.offset-m2.s12
      [:div.card-panel
       [:h3.light "Apply for a Home"]

       [:p.flow-text-small
        "To get started, please create an account so we can save your application."]

       (for [error errors]
         [:div.alert.alert-error [:p.alert-text error]])

       [:form {:action "/signup" :method "POST"}

        ;; TODO: abstract this
        [:div.row
         [:div.col.s6.input-field
          [:input#first-name.validate {:type     "text"
                                       :name     "first-name"
                                       :required true
                                       :value    first-name}]
          [:label {:for "first-name"} "First name"]]

         [:div.col.s6.input-field
          [:input#last-name.validate {:type     "text"
                                       :name     "last-name"
                                       :required true
                                       :value    last-name}]
          [:label {:for "last-name"} "Last name"]]]

        [:div.row
         [:div.input-field.col.s12
          [:input#email.validate {:type     "email"
                                  :name     "email"
                                  :required true
                                  :value    email}]
          [:label {:for "email"} "Email"]]]

        [:div.row
         [:div.input-field.col.s12
          [:input#password-1.validate {:type     "password"
                                       :required true
                                       :name     "password-1"}]
          [:label {:for "password-1"} "Password"]]]

        [:div.row
         [:div.input-field.col.s12
          [:input#password-2.validate {:type     "password"
                                       :required true
                                       :name     "password-2"}]
          [:label {:for "password-2"} "Re-enter password"]]]

        [:div.row
         [:div.col.s12.center-align
          [:button.btn.waves-effect.waves-light.btn-large.star-green.lighten-1 {:type "submit"}
           [:span.hide-on-small-only "Get Started"]
           [:span.hide-on-med-and-up "Begin"]
           [:i.material-icons.right "send"]]]]

        [:div.divider]

        [:div.row.panel-footer.valign-wrapper
         [:div.col.s6
          [:p "Already have an account?"]]
         [:div.col.s6
          [:a.right.btn.white.star-orange-text.waves-effect.waves-light
           {:href "/login"}
           "Log In"]]]]]]]]])

;; =============================================================================
;; API
;; =============================================================================

(defn signup
  "The signup view."
  [email first-name last-name errors ]
  (base
   :title "Sign Up"
   :content (signup-content email first-name last-name errors )))

(defn invalid-activation
  []
  (base
   :content [:main
             [:div.container
              [:h3 "Oops!"]
              [:p.flow-text "Your activation link is invalid, or has expired."]]]))

(defn signup-complete
  []
  (base
   :content [:main
             [:div.container
              [:h3 "Thanks for creating an account!"]
              [:p.flow-text "Please look out for an activation email in your inbox."]
              [:p.flow-text "To continue the application, click the activation link found in that email."]]]))
