(ns starcity.pages.auth
  (:require [starcity.pages.base :refer [base]]
            [starcity.pages.util :refer [ok malformed]]
            [starcity.models.account :as account]
            [starcity.middleware :refer [get-component]]
            [starcity.util :refer :all]
            [buddy.auth :refer [authenticated?]]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [clojure.string :refer [trim]]
            [ring.util.response :as response]))

;; =============================================================================
;; Constants

(def ^{:private true} REDIRECT-AFTER-LOGIN "/me")

;; =============================================================================
;; Components

(defn- content
  [req errors email next-url]
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

(defn- render-login [req & {:keys [errors email] :or {errors [] email ""}}]
  ;; NOTE: Preserves the next url through the POST req by using a hidden input
  (let [next-url (get-in req [:params :next] REDIRECT-AFTER-LOGIN)]
    (base (content req errors email next-url) :css ["signin.css"])))

;; =============================================================================
;; Authentication

(defn- required
  [message]
  [v/required :message message])

(defn- validate-credentials
  [credentials]
  (b/validate
   credentials
   {:email    [(required "An email address is required.")
               [v/email :message "That email address is invalid."]]
    :password [(required "A password is required.")
               [v/min-count 8 :message "Your password should be at least 8 characters long."]]}))

(defn- errors-from
  "Extract errors from a bouncer error map."
  [[errors _]]
  (reduce (fn [acc [_ es]] (concat acc es)) [] errors))

(defn- valid?
  [[errors result]]
  (if (nil? errors)
    result
    false))

(defn- clean-credentials
  [credentials]
  (-> (update credentials :email trim)
      (update :password trim)))

(defn- authenticate
  "Authenticate the user by checking email and password."
  [{:keys [params session] :as req}]
  (let [db      (get-component req :db)
        vresult (-> params clean-credentials validate-credentials)]
    (if-let [{:keys [email password]} (valid? vresult)]
      (if-let [user (account/authenticate db email password)]
        ;; success
        (let [next-url (get-in req [:params :next])
              session  (assoc session :identity user)]
          (-> (response/redirect next-url)
              (assoc :session session)))
        ;; authentication failure
        (malformed (render-login req
                                 :errors ["The credentials you entered are invalid; please try again."]
                                 :email email))) ; TODO: Need more here?
      ;; validation failure
      (malformed (render-login req :errors (errors-from vresult) :email (:email params))))))

;; =============================================================================
;; API

(defn handle-login [req]
  (case (:request-method req)
    :get  (if (authenticated? req)
            (response/redirect "/me")
            (ok (render-login req)))
    :post (authenticate req)
    (ok (render-login req))))

(defn handle-logout [req]
  (-> (response/redirect "/login")
      (assoc :session {})))
