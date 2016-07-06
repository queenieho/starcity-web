(ns starcity.views.auth.signup
  (:require [starcity.views.base :refer [base]]
            [clojure.string :as s]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- form-group
  [{:keys [id value type placeholder] :or {type "text" value ""}}]
  (let [placeholder (or placeholder (-> id s/capitalize (s/replace "-" " ")))
        attrs       {:name        id
                     :id          id
                     :type        type
                     :required    true
                     :value       value
                     :placeholder placeholder}]
    [:div
     [:label.sr-only {:for id} placeholder]
     [:input.form-control attrs]]))

;; =============================================================================
;; API
;; =============================================================================

(defn signup
  "The content for the signup page."
  [errors email first-name last-name]
  (let [inputs [{:id "first-name" :type "text" :value first-name}
                {:id "last-name" :type "text" :value last-name}
                {:id "email" :type "email" :value email :placholder "Email address"}
                {:id "password-1" :type "password" :placeholder "Password"}
                {:id "password-2" :type "password" :placeholder "Re-enter Password"}]]
    (base
     [:div.container
      [:div.row
       [:form.form-signup {:action "/signup" :method "post"}
        [:h2.form-signup-heading "Sign Up"]
        (for [e errors]
          [:div.alert.alert-danger {:role "alert"} e])

        (map form-group inputs)

        [:button.btn.btn-lg.btn-success.btn-block {:type "submit"} "Create Account"]
        [:p.text-center "or"]
        [:a.btn.btn-primary.btn-block {:href "/login"} "Log In"]]]]
     :css ["signup.css"])))

(defn invalid-activation
  []
  (base
   [:div.container
    [:div.page-header
     [:h1 "Oops!"]]
    [:p.lead "Your activation link is invalid, or has expired."]]))

(defn signup-complete
  []
  (base
   [:div.container
    [:div.page-header
     [:h1 "Thanks for signing up!"]]
    [:p.lead "Please check your inbox for an activation link."]]))
