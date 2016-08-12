(ns starcity.views.auth
  (:require [starcity.views.base :refer [base]]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- content
  [errors]
  [:main.auth
   [:div.container
    [:div.row
     [:div.col.l6.offset-l3.m8.offset-m2.s12
      [:div.card-panel
       [:h3.light "Forgotten Password"]
       [:p.flow-text-small "Enter your email below, and we'll send you a new password."]

       (for [error errors]
         [:div.alert.alert-error
          [:p.alert-text error]])

       [:form {:action "/forgot-password" :method "POST"}
        [:div.row
         [:div.input-field.col.s12
          [:input#email.validate {:type      "email"
                                  :name      "email"
                                  :required  true
                                  :autofocus true}]
          [:label {:for "email"} "Email"]]]

        [:div.row
         [:div.col.s12.center-align
          [:button.btn.waves-effect.waves-light.btn-large.star-green.lighten-1 {:type "submit"}
           "Reset Password"
           [:i.material-icons.right "send"]]]] ]

       [:div.divider]

       [:div.row.panel-footer.valign-wrapper
        [:div.col.s6
         [:p "Know your password?"]]
        [:div.col.s6
         [:a.right.btn.white.star-orange-text.waves-effect.waves-light
          {:href "/login"}
          "Log In"]]]]]]]])

;; =============================================================================
;; API
;; =============================================================================

(defn forgot-password
  [& {:keys [errors] :or {errors []}}]
  (base :title "Forgotten Password"
        :content (content errors)))
