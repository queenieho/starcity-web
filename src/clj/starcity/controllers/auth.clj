(ns starcity.controllers.auth
  (:require [starcity.views.auth :as view]
            [hiccup.core :refer [html]]
            [ring.util.response :as response]
            [ring.util.codec :refer [url-encode]]
            [starcity.controllers.utils :refer :all]
            [starcity.datomic :refer [conn]]
            [starcity.config :refer [config]]
            [starcity.services.mailgun :refer [send-email]]
            [clojure.string :as str]
            [datomic.api :as d]
            [starcity.models.account :as account]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- show-error
  [msg]
  (malformed (view/forgot-password :errors [msg])))

(defn- send-password-reset-email
  [{:keys [:account/email :account/first-name] :as acct} new-password]
  (send-email email "Password Reset"
              (html
               [:body
                [:p (format "Hi %s," first-name)]
                [:p "As requested, we have reset your password. Your new password is:"]
                [:p [:b new-password]]
                [:p
                 "After logging in "
                 [:a {:href (format "%s/login?email=%s" (:hostname config) (url-encode email))} "here"]
                 ", please change your password to something more memorable by clicking on "
                 [:b "My Account"]
                 " in the upper right-hand corner of the page."]
                [:p "If this was not you, please contact us by replying to this email."]
                [:p "Best,"
                 [:br]
                 [:br]
                 "Mo"
                 [:br]
                 "Head of Community"]])))

;; =============================================================================
;; API
;; =============================================================================

(defn logout! [_]
  (-> (response/redirect "/login")
      (assoc :session {})))

(defn show-forgot-password
  [req]
  (ok (view/forgot-password)))

(defn forgot-password!
  [{:keys [params] :as request}]
  (if-let [email (:email params)]
    (let [cleaned (-> email str/trim str/lower-case)]
      (if-let [acct (account/by-email cleaned)]
        (let [new-password (account/reset-password! (:db/id acct))
              next         (format "/login?email=%s&reset-password=true" cleaned)]
          (send-password-reset-email acct new-password)
          (response/redirect next))
        (show-error (format "We do not have an account under '%s'. Please try again, or create an account."
                            cleaned))))
    (show-error "Please enter your email.")))
