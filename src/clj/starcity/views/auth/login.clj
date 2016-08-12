(ns starcity.views.auth.login
  (:require [starcity.views.base :refer [base]]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- login-content
  [errors email {:keys [next reset-password] :as params}]
  [:main#login.auth
   [:div.container
    [:div.row
     [:div.col.l6.offset-l3.m8.offset-m2.s12
      [:div.card-panel
       [:h3.light "Log in"]

       (for [error errors]
         [:div.alert.alert-error
          [:p.alert-text error]])

       (when reset-password
         [:div.alert.alert-success
          [:p.alert-text "Password reset! Please check your inbox."]])

       [:form {:action "/login" :method "POST"}
        [:input {:type "hidden" :name "next" :value next}]

        [:div.row
         [:div.input-field.col.s12
          [:input#email.validate {:type      "email"
                                  :name      "email"
                                  :required  true
                                  :autofocus (when (= email "") true)
                                  :value     email}]
          [:label {:for "email"} "Email"]]]

        [:div.row
         [:div.input-field.col.s12
          [:input#password.validate {:type      "password"
                                     :required  true
                                     :name      "password"}]
          [:label {:for "password"} "Password"]]]

        [:div.row
         [:div.col.s12.center-align
          [:button.btn.waves-effect.waves-light.btn-large.star-green.lighten-1 {:type "submit"}
           "Sign In"
           [:i.material-icons.right "send"]]]]

        [:div.row
         [:div.col.s12.center-align
          [:a {:href "/forgot-password"} "Forgotten password?"]]]

        [:div.divider]

        [:div.row.panel-footer.valign-wrapper
         [:div.col.s6
          [:p "Don't have an account?"]]
         [:div.col.s6
          [:a.right.btn.white.star-orange-text.waves-effect.waves-light
           {:href "/signup"}
           "Begin"]]]]]]]]])

;; =============================================================================
;; API
;; =============================================================================

(defn login
  "The login view."
  [errors email params]
  (base
   :title "Log In"
   :content (login-content errors email params)))
