(ns starcity.routes
  (:require [buddy.auth :refer [authenticated?]]
            [buddy.auth.accessrules :refer [restrict]]
            [compojure
             [core :refer [ANY context defroutes GET POST routes]]
             [route :as route]]
            [ring.util.response :as response]
            [starcity.auth :refer [authenticated-user user-isa]]
            [starcity.api.plaid :as plaid]
            [starcity.controllers
             [application :as application]
             [auth :as auth]
             [communities :as communities]
             [faq :as faq]
             [landing :as landing]
             [register :as register]
             [terms :as terms]
             [privacy :as privacy]]
            [starcity.controllers.application
             [personal :as personal]
             [logistics :as logistics]
             [community-fitness :as community-fitness]
             [submit :as submit]]
            [starcity.controllers.auth
             [login :as login]
             [signup :as signup]]
            [clojure.pprint :as pprint]))

;; NOTE: If an user is currently listed as an applicant, he/she should only be
;; able to access the /application endpoint; similarly, users listed as tenants
;; should not be allowed to access the /application endpoint (only the dashboard)
;; The `redirect-on-invalid-authorization' handler is to enforce this behavior.
;; It's likely that there's a better way to do this.

(defn- redirect-on-invalid-authorization
  [to]
  (fn [req msg]
    (if (authenticated? req)
      (response/redirect to)
      (response/redirect "/"))))

(defn- wrap-log-response
  [handler]
  (fn [req]
    (let [res (handler req)]
      (pprint/pprint res)
      res)))

;; =============================================================================
;; API
;; =============================================================================

(defroutes app-routes
  ;; public
  (GET "/" [] landing/show-landing)
  (GET "/register"     [] register/register-user!)
  (GET "/communities" [] communities/show-communities)
  (GET "/fack"          [] faq/show-faq)
  (GET "/terms"         [] terms/show-terms)
  (GET "/privacy"        [] privacy/show-privacy)

  (GET  "/login"        [] login/show-login)
  (POST "/login"        [] login/login!)

  (ANY  "/logout"       [] auth/logout!)

  (context "/signup" []
    (GET   "/"         [] signup/show-signup)
    (POST  "/"         [] signup/signup!)
    (GET   "/complete" [] signup/show-complete)
    (GET   "/activate" [] signup/activate!))

  ;; auth
  (context "/application" []
    (restrict
     (routes
      (GET "/" [] application/show-application)

      (restrict
       (routes
        (GET "/logistics" [] logistics/show-logistics)
        (POST "/logistics" [] logistics/save!))
       logistics/restrictions)

      (restrict
       (routes
        (GET "/personal" [] personal/show-personal)
        (POST "/personal" [] personal/save!))
       personal/restrictions)

      (restrict
       (routes
        (GET "/community" [] community-fitness/show-community-fitness)
        (POST "/community" [] community-fitness/save!))
       community-fitness/restrictions)

      (restrict
       (routes
        (GET "/submit" [] submit/show-submit)
        (POST "/submit" [] submit/submit!))
       submit/restrictions))

     {:handler  {:and [authenticated-user (user-isa :account.role/applicant)]}
      :on-error (redirect-on-invalid-authorization "/me")}))

  ;; (GET "/me" [] (-> dashboard/show-dashboard
  ;;                   (restrict {:handler  {:and [authenticated-user (user-isa :account.role/tenant)]}
  ;;                              :on-error (redirect-on-invalid-authorization "/application")})))

  (context "/api/v1" []
    (restrict
     (routes
      (POST "/plaid/auth" [] plaid/authenticate!))
     {:handler {:and [authenticated-user]}}))

  (context "/webhooks" []
    (POST "/plaid" [] plaid/hook))

  ;; catch-all
  (route/not-found "<p>Not Found</p>"))
