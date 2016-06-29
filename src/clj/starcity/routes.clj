(ns starcity.routes
  (:require [buddy.auth :refer [authenticated?]]
            [buddy.auth.accessrules :refer [restrict]]
            [compojure
             [core :refer [ANY context defroutes GET POST routes]]
             [route :as route]]
            [ring.util.response :as response]
            [starcity.auth :refer [authenticated-user user-isa]]
            [starcity.controllers
             [application :as application]
             [auth :as auth]
             [availability :as availability]
             [dashboard :as dashboard]
             [faq :as faq]
             [landing :as landing]
             [register :as register]]
            [starcity.controllers.application
             [checks :as checks]
             [logistics :as logistics]]
            [starcity.controllers.auth
             [login :as login]
             [signup :as signup]]))

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

      (restrict
       (routes
        (GET "/checks" [] checks/show-checks)
        (POST "/checks" [] checks/save!))
       checks/restrictions))

     {:handler  {:and [authenticated-user (user-isa :account.role/applicant)]}
      :on-error (redirect-on-invalid-authorization "/me")}))

  (GET "/me" [] (-> dashboard/show-dashboard
                    (restrict {:handler  {:and [authenticated-user (user-isa :account.role/tenant)]}
                               :on-error (redirect-on-invalid-authorization "/application")})))

  ;; catch-all
  (route/not-found "<p>Not Found</p>"))
