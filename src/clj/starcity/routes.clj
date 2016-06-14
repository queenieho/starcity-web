(ns starcity.routes
  (:require [compojure.core :refer [defroutes routes context GET POST ANY]]
            [compojure.route :as route]
            [starcity.pages.landing :as landing]
            [starcity.pages.register :as register]
            [starcity.pages.auth :as auth]
            [starcity.pages.auth.login :as login]
            [starcity.pages.auth.signup :as signup]
            [starcity.pages.application :as application]
            [starcity.pages.dashboard :as dashboard]
            [starcity.pages.availability :as availability]
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
  (GET  "/register"     [] register/register-user)
  (GET  "/availability" [] availability/render)

  (GET  "/login"        [] login/render)
  (POST "/login"        [] login/login!)

  (ANY  "/logout"       [] auth/logout!)

  (context "/signup" []
    (GET   "/"         [] signup/render)
    (POST  "/"         [] signup/signup!)
    (GET   "/complete" [] signup/render-complete)
    (GET   "/activate" [] signup/activate!))

  ;; auth
  (context "/application" []
    (restrict
     (routes
      (GET "/" [] application/render))
     {:handler  {:and [authenticated-user
                       (user-isa :account.role/applicant)]}
      :on-error (redirect-on-invalid-authorization "/me")}))
  (GET "/me" [] (-> dashboard/render
                    (restrict {:handler  {:and [authenticated-user (user-isa :account.role/tenant)]}
                               :on-error (redirect-on-invalid-authorization "/application")})))

  ;; catch-all
  (ANY "*" [] "<p>Not found</p>"))
