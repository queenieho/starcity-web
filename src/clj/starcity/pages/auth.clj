(ns starcity.pages.auth
  (:require [starcity.pages.base :refer [base]]
            [starcity.pages.util :refer [ok]]
            [ring.util.response :as response]))

;; =============================================================================
;; Constants

(def ^{:private true} REDIRECT-AFTER-LOGIN "/me")

;; =============================================================================
;; Components

(defn- content [req]
  [:div.container
   [:form.form-signin {:action "/login" :method "post"}
    [:h2.form-signin-heading "Please sign in"]
    [:label.sr-only {:for "inputEmail"} "Email address"]
    [:input#input-email.form-control
     {:name "email" :type "email" :placeholder "Email address" :required true :autofocus true}]
    [:label.sr-only {:for "inputPassword"} "Password"]
    [:input#input-password.form-control
     {:name "password" :type "password" :placeholder "Password" :required true}]
    [:button.btn.btn-lg.btn-primary.btn-block {:type "submit"} "Sign in"]]])

(defn- login-view [req]
  (base (content req) :css ["signin.css"]))

;; =============================================================================
;; Authentication

;; TODO: Actually look up and store the password in db
(def ^{:private true} authdata
  {"test@test.com" "password"})

(defn- authenticate
  "Authenticate the user by checking email and password."
  [{:keys [params session] :as req}]
  (let [username       (:email params)
        password       (:password params)
        found-password (get authdata username)]
    (if (and found-password (= found-password password))
      (let [next-url (get-in req [:query-params :next] REDIRECT-AFTER-LOGIN)
            session' (assoc session :identity (keyword username))]
        (-> (response/redirect next-url)
            (assoc :session session')))
      (ok (login-view req)))))

;; =============================================================================
;; API

(defn handle-login [req]
  (case (:request-method req)
    :get  (ok (login-view req))
    :post (authenticate req)
    (ok (login-view req))))

(defn handle-logout [req]
  (-> (response/redirect "/login")
      (assoc :session {})))
