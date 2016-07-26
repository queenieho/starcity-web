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
     [:div.col.s6.offset-s3
      [:div.card-panel
       [:h4.light "Sign up"]

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
          [:input#first-name.validate {:type     "text"
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
           "Sign Up"
           [:i.material-icons.right "send"]]]]

        [:div.divider]

        [:div.row.panel-footer
         [:div.col.s8
          [:p "Already have an account?"]]
         [:div.col.s4
          [:a.btn.white.star-orange-text.waves-effect.waves-light {:href "/login"}
           "Sign in"]]]]]]]]])

;; =============================================================================
;; API
;; =============================================================================

(defn signup
  "The signup view."
  [email first-name last-name errors ]
  (base
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
              [:h3 "Thanks for signing up!"]
              [:p.flow-text "Please check your inbox for an activation link."]]]))
