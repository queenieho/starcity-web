(ns starcity.views.bulma.login
  (:require [starcity.views.page :as p]
            [starcity.views.components
             [hero :as h]
             [button :as b]
             [layout :as l]
             [form :refer [label control]]]
            [hiccup.form :as f]))


;; =============================================================================
;; Components
;; =============================================================================

(defn- login-form
  [{:keys [next reset-password email]}]
  (let [focus-email? (= email "")]
    (f/form-to
     [:post "/login"]
     (f/hidden-field "next" next)
     (label "email" "Email")
     (control
      (f/email-field {:class "input" :id "email" :required true :autofocus focus-email?} "email" email))
     (label "password" "Password")
     (control
      (f/password-field {:class "input" :id "password" :required true} "password"))
     ;; TODO: SCSS
     (control {:style "margin-top: 20px;"}
      (b/primary {:class "is-medium"} :submit "Log in"))

     [:hr {:style "margin-top: 20px;"}]

     (control
      [:div.level
       [:div.level-left
        [:div.level-item
         (b/link {:href "/forgot-password"} "Forgotten password?")]]
       [:div.level-right
        [:div.level-item
         (b/button {:class "is-success" :href "/signup"} :link "Create Account")]]]))))

(defn- hero-section [{:keys [params]}]
  (h/hero
   {:class "is-fullheight is-primary is-bold"} ; TODO: is hero the right thing for this?
   (h/head (p/navbar-inverse))
   (h/body
    [:div.container
     [:div.columns
      [:div.column.is-half.is-offset-one-quarter
       (l/box
        ;; TODO: SCSS
        [:h3.is-3.title {:style "color: black;"} "Log in to Starcity"]
        (login-form params))]]])))

;; =============================================================================
;; API
;; =============================================================================

(def login
  (p/page (p/title "Log In") (p/content hero-section)))
