(ns starcity.views.auth
  (:require [starcity.views.page :as p]
            [starcity.views.templates.simple :as simple]
            [starcity.views.components
             [hero :as h]
             [layout :as l]
             [button :as b]
             [notification :as n]
             [form :refer [label control]]]
            [starcity.views.utils :refer [errors-from]]
            [hiccup.form :as f]))

;; =============================================================================
;; Components
;; =============================================================================

(def ^:private forgotten-password-form
  (f/form-to
   [:post "/forgot-password"]
   (control
    {:class "has-icon"}
    (f/email-field {:class       "input is-medium"
                    :id          "email"
                    :required    true
                    :autofocus   true
                    :placeholder "email address"}
                   "email")
    [:i.fa.fa-envelope])
   (control
    {:style "margin-top: 20px;"}
    [:button.button.is-white.is-outlined.is-large {:type "submit"}
     "Reset"])))

(def ^:private content
  (simple/info
   (simple/body
    (simple/title "Forgotten password?")
    (simple/subtitle "Enter your email and we'll send you a new one.")
    forgotten-password-form)
   (simple/foot
    ["/login" "Log In"]
    ["/signup" "Create an Account"])))

;; =============================================================================
;; API
;; =============================================================================

(def forgot-password
  (p/page
   (p/title "Forgotten Password")
   (p/content content)))
