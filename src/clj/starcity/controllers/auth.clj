(ns starcity.controllers.auth
  (:require [clojure.string :as str]
            [hiccup.core :refer [html]]
            [ring.util
             [codec :refer [url-encode]]
             [response :as response]]
            [starcity.config :refer [hostname]]
            [starcity.controllers.utils :refer :all]
            [starcity.models.account :as account]
            [starcity.services.mailgun :as mailgun]
            [starcity.views.auth :as view]
            [starcity.web.messages :refer [respond-with-errors]]
            [starcity.auth :as auth]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- send-password-reset-email
  [{:keys [:account/email :account/first-name] :as acct} new-password]
  (mailgun/send-email email "Password Reset"
                      (html
                       [:body
                        [:p (format "Hi %s," first-name)]
                        [:p "As requested, we have reset your password. Your new password is:"]
                        [:p [:b new-password]]
                        [:p
                         "After logging in "
                         [:a {:href (format "%s/login?email=%s&next=/account" hostname (url-encode email))} "here"]
                         ", please change your password to something more memorable by clicking on "
                         [:b "My Account"]
                         " in the upper right-hand corner of the page."]
                        [:p "If this was not you, please contact us by replying to this email."]
                        mailgun/default-signature])))

;; =============================================================================
;; API
;; =============================================================================

(defn logout
  "Log the user out by clearing the session."
  [_]
  (-> (response/redirect "/login")
      ;; NOTE: Must assoc `nil` into the session for this to work. Seems weird
      ;; to have different behavior when a key has a value of `nil` than for
      ;; when a key is not present. Given what `nil` means, these should be the
      ;; same? Perhaps submit a PR?
      (assoc :session nil)))

(def show-forgot-password
  (comp ok view/forgot-password))

(defn forgot-password
  [{:keys [params] :as req}]
  (if-let [email (:email params)]
    (let [cleaned (-> email str/trim str/lower-case)]
      (if-let [acct (account/by-email cleaned)]
        (let [new-password (account/reset-password (auth/requester req))
              next         (format "/login?email=%s&reset-password=true" cleaned)]
          (send-password-reset-email acct new-password)
          (response/redirect next))
        (respond-with-errors req (format "We do not have an account under %s. Please try again, or create an account."
                                         cleaned) view/forgot-password)))
    (respond-with-errors req "Please enter your email." view/forgot-password)))
