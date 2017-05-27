(ns starcity.controllers.auth
  (:require [clojure.string :as str]
            [customs.auth :as auth]
            [datomic.api :as d]
            [facade.core :as facade]
            [net.cgrand.enlive-html :as html]
            [ring.util
             [codec :refer [url-encode]]
             [response :as response]]
            [starcity
             [config :as config]
             [datomic :refer [conn]]]
            [starcity.controllers.common :as common]
            [starcity.models.account :as account]
            [starcity.services.mailgun :as mail]
            [starcity.services.mailgun.message :as mm]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- send-password-reset-email
  [account new-password]
  (let [email (account/email account)]
    (mail/send
     email
     "Starcity Password Reset"
     (mm/msg
      (mm/greeting (account/first-name account))
      (mm/p "As requrested, we have reset your password. Your temporary password is:")
      (mm/p (str "<b>" new-password "</b>"))
      (mm/p
       (format "After logging in <a href='%s'>here</a>, please change your
       password to something more memorable by clicking on <b>My Account</b> in the upper right-hand corner of the page."
               (format "%s/login?email=%s&next=/account" config/hostname (url-encode email))))
      (mm/p "If this was not you, please contact us at <a href='mailto:team@joinstarcity.com>team@joinstarcity.com</a>.")
      (mm/signature)))))

;; =============================================================================
;; Views
;; =============================================================================

(html/defsnippet forgot-password-main "templates/forgot-password.html" [:main]
  [& errors]
  [:div.alerts] (facade/maybe-errors errors))

;; =============================================================================
;; Handlers
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

(defn- forgot-password-errors
  [req & errors]
  (common/render-malformed
   (facade/public req
                  :css-bundles ["public.css"]
                  :js-bundles ["main.js"]
                  :main (apply forgot-password-main errors))))

(defn show-forgot-password [req]
  (common/render-ok
   (facade/public req
                  :css-bundles ["public.css"]
                  :js-bundles ["main.js"]
                  :main (forgot-password-main))))

(defn forgot-password
  [{:keys [params] :as req}]
  (if-let [email (:email params)]
    (let [cleaned (-> email str/trim str/lower-case)]
      (if-let [account (account/by-email (d/db conn) cleaned)]
        (let [[new-password tx-data] (auth/reset-password account)
              next                   (format "/login?email=%s&reset-password=true" cleaned)]
          @(d/transact conn [tx-data])
          (send-password-reset-email account new-password)
          (response/redirect next))
        (forgot-password-errors req (format "We do not have an account under %s. Please try again, or create an account."
                                            cleaned))))
    (forgot-password-errors req "Please enter your email address.")))
