(ns starcity.controllers.auth
  (:require [blueprints.models.account :as account]
            [blueprints.models.events :as events]
            [clojure.string :as str]
            [datomic.api :as d]
            [facade.core :as facade]
            [net.cgrand.enlive-html :as html]
            [ring.util.response :as response]
            [starcity.controllers.common :as common]
            [starcity.datomic :refer [conn]]))

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
   (common/page req {:css-bundles ["public.css"]
                     :js-bundles  ["main.js"]
                     :main        (apply forgot-password-main errors)})))


(defn show-forgot-password [req]
  (common/render-ok
   (common/page req {:css-bundles ["public.css"]
                     :js-bundles  ["main.js"]
                     :main        (forgot-password-main)})))


(defn forgot-password
  [{:keys [params] :as req}]
  (if-let [email (:email params)]
    (let [cleaned (-> email str/trim str/lower-case)]
      (if-let [account (account/by-email (d/db conn) cleaned)]
        (let [next (format "/login?email=%s&reset-password=true" cleaned)]
          @(d/transact conn [(events/reset-password account)])
          (response/redirect next))
        (forgot-password-errors req (format "We do not have an account under %s. Please try again, or create an account."
                                            cleaned))))
    (forgot-password-errors req "Please enter your email address.")))
