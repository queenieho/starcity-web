(ns starcity.views.signup
  (:require [starcity.views.page :as p]
            [starcity.views.templates.simple :as simple]
            [starcity.views.components
             [hero :as h]
             [layout :as l]
             [button :as b]
             [notification :as n]
             [form :refer [label control]]]
            [starcity.views.signup.complete]
            [starcity.views.signup.invalid-activation]
            [potemkin :refer [import-vars]]
            [starcity.views.utils :refer [errors-from]]
            [hiccup.form :as f]
            [clojure.string :as s]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- signup-form
  [{:keys [email first-name last-name errors]}]
  (f/form-to
   [:post "/signup"]

   ;; Name
   [:div.control.is-grouped
    (control
     {:class "is-expanded has-icon"}
     (f/text-field {:required    true
                    :class       "input is-medium"
                    :placeholder "first name"}
                   "first-name"
                   first-name)
     [:i.fa.fa-user])
    (control
     {:class "is-expanded"}
     (f/text-field {:required    true
                    :class       "input is-medium"
                    :placeholder "last name"}
                   "last-name"
                   last-name))]

   ;; Email
   (control
    {:class "is-expanded has-icon"}
    (f/email-field {:required    true
                    :class       "input is-medium"
                    :placeholder "email address"}
                   "email"
                   email)
    [:i.fa.fa-envelope])

   ;; Password
   [:div.control.is-grouped
    (control
     {:class "is-expanded has-icon"}
     (f/password-field {:required    true
                        :class       "input is-medium"
                        :placeholder "desired password"}
                       "password-1")
     [:i.fa.fa-key])
    (control
     {:class "is-expanded"}
     (f/password-field {:required true
                        :class    "input is-medium"
                        :placeholder "re-enter password"}
                       "password-2"))]

   (simple/button "Create Account")))

;; =============================================================================
;; API
;; =============================================================================

(import-vars
 [starcity.views.signup.complete
  complete]
 [starcity.views.signup.invalid-activation
  invalid-activation])

(def signup
  (p/page
   (p/title "Sign Up")
   (simple/success
    (simple/body
     (simple/title "Sign Up")
     (simple/subtitle "To begin your "
                      [:strong "member application"]
                      ", you'll first need to create an account.")
     (comp signup-form :params))
    (simple/foot
     ["/login" "Log In"]
     ["/forgot-password" "Forgotten password?"]))))
