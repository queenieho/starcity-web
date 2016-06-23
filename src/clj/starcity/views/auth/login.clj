(ns starcity.views.auth.login)

(defn login
  "The login view content."
  [errors email next-url]
  [:div.container
   [:form.form-signin {:action "/login" :method "post"}
    [:h2.form-signin-heading "Please sign in"]
    (for [e errors]
      [:div.alert.alert-danger {:role "alert"} e])
    [:input {:type "hidden" :name "next" :value next-url}]
    [:label.sr-only {:for "inputEmail"} "Email address"]
    [:input#input-email.form-control
     {:name        "email"
      :type        "email"
      :placeholder "Email address"
      :required    true
      :autofocus   (when (= email "") true)
      :value       email}]
    [:div.form-group
     [:label.sr-only {:for "inputPassword"} "Password"]
     [:input#input-password.form-control
      {:name "password" :type "password" :placeholder "Password" :required true
       :autofocus (when (not= email "") true)}]]
    [:button.btn.btn-lg.btn-primary.btn-block {:type "submit"} "Sign in"]]])
