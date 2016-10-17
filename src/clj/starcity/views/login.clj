(ns starcity.views.login
  (:require [hiccup.form :as f]
            [starcity.views.components.form :refer [control]]
            [starcity.views.page :as p]
            [starcity.views.templates.simple :as simple]))

;; =============================================================================
;; Components
;; =============================================================================

(defn- login-form
  [{:keys [next reset-password email]}]
  (let [focus-email? (empty? email)]
    (f/form-to
     [:post "/login"]
     (f/hidden-field "next" next)
     ;; email
     (control
      {:class "has-icon"}
      (f/email-field {:class       "input is-medium"
                      :id          "email"
                      :required    true
                      :autofocus   focus-email?
                      :placeholder "email address"}
                     "email" email)
      [:i.fa.fa-envelope])

     ;; password
     (control
      {:class "has-icon"}
      (f/password-field {:class       "input is-medium"
                         :id          "password"
                         :required    true
                         :placeholder "password"}
                        "password")
      [:i.fa.fa-key])

     (simple/button "Log in"))))

(def ^:private content
  (simple/primary
   (simple/body
    (simple/title "Log In")
    (comp login-form :params))
   (simple/foot
    ["/forgot-password" "Forgotten password?"]
    ["/signup" "Create an Account"])))

;; =============================================================================
;; API
;; =============================================================================

(def login
  (p/page (p/title "Log In") content))
