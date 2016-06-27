(ns starcity.routes
  (:require [compojure.core :refer [defroutes routes context GET POST ANY]]
            [compojure.route :as route]
            [starcity.controllers.landing :as landing]
            [starcity.controllers.register :as register]
            [starcity.controllers.availability :as availability]
            [starcity.controllers.faq :as faq]
            [starcity.controllers.auth :as auth]
            [starcity.controllers.auth.login :as login]
            [starcity.controllers.auth.signup :as signup]
            [starcity.controllers.application :as application]
            [starcity.controllers.application.logistics :as logistics]
            [starcity.controllers.application.checks :as checks]
            [starcity.controllers.dashboard :as dashboard]
            [starcity.auth :refer [authenticated-user unauthorized-handler user-isa]]
            [buddy.auth :refer [authenticated?]]
            [buddy.auth.accessrules :refer [restrict]]
            [ring.util.response :as response]))

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

;; =============================================================================
;; API
;; =============================================================================

(defroutes app-routes
  ;; public
  (GET "/" [] landing/show-landing)
  (GET  "/register"     [] register/register-user!)
  (GET  "/availability" [] availability/show-availability)
  (GET "/fack"          [] faq/show-faq)

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

      (GET "/logistics" [] logistics/show-logistics)
      (POST "/logistics" [] logistics/save!)

      (GET "/checks" [] checks/show-checks)
      (POST "/checks" [] checks/save!))
     {:handler  {:and [authenticated-user
                       (user-isa :account.role/applicant)]}
      :on-error (redirect-on-invalid-authorization "/me")}))

  (GET "/me" [] (-> dashboard/show-dashboard
                    (restrict {:handler  {:and [authenticated-user (user-isa :account.role/tenant)]}
                               :on-error (redirect-on-invalid-authorization "/application")})))

  ;; catch-all
  (route/not-found "<p>Not Found</p>"))
