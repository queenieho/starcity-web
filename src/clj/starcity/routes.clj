(ns starcity.routes
  (:require [buddy.auth :refer [authenticated?]]
            [buddy.auth.accessrules :refer [restrict]]
            [compojure
             [core :refer [ANY context defroutes GET POST routes]]
             [route :as route]]
            [ring.util.response :as response]
            [starcity.auth :refer [authenticated-user user-isa]]
            [starcity.api.plaid :as plaid]
            [starcity.api.admin.applications :as api-applications]
            [starcity.controllers
             [account :as account]
             [application :as application]
             [auth :as auth]
             [communities :as communities]
             [admin :as admin]
             [faq :as faq]
             [landing :as landing]
             [register :as register]
             [terms :as terms]
             [privacy :as privacy]
             [team :as team]
             [about :as about]]
            [starcity.controllers.application
             [personal :as personal]
             [logistics :as logistics]
             [community-fitness :as community-fitness]
             [submit :as submit]]
            [starcity.controllers.auth
             [login :as login]
             [signup :as signup]]))

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
  (GET "/register"     [] register/register-user!)
  (GET "/communities" [] communities/show-communities)
  (GET "/faq"          [] faq/show-faq)
  (GET "/terms"         [] terms/show-terms)
  (GET "/privacy"        [] privacy/show-privacy)
  (GET "/about" [] about/show-about)
  (GET "/team" [] team/show-team)

  (GET "/forgot-password" [] auth/show-forgot-password)
  (POST "/forgot-password" [] auth/forgot-password!)

  (GET  "/login"        [] login/show-login)
  (POST "/login"        [] login/login!)

  (context "/account" []
    (restrict
     (routes
      (GET "/" [] account/show-account-settings)
      (POST "/password" [] account/update-password!))
     {:handler  authenticated-user
      :on-error (redirect-on-invalid-authorization "/")}))

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

  (context "/admin" []
    (restrict
     (routes
      (GET "*" [] admin/show))
     {:handler  {:and [authenticated-user (user-isa :account.role/admin)]}
      :on-error (redirect-on-invalid-authorization "/")}))

  ;; (GET "/me" [] (-> dashboard/show-dashboard
  ;;                   (restrict {:handler  {:and [authenticated-user (user-isa :account.role/tenant)]}
  ;;                              :on-error (redirect-on-invalid-authorization "/application")})))

  (context "/api/v1" []
    (restrict
     (routes
      (POST "/plaid/auth" [] plaid/authenticate!)

      (context "/admin" []
        (restrict
         (routes
          (GET "/applications" [] api-applications/fetch-applications)
          (GET "/applications/:application-id" [] api-applications/fetch-application)

          (GET "/income-file/:file-id" [] api-applications/fetch-income-file))
         {:handler  {:and [(user-isa :account.role/admin)]}
          :on-error (fn [_ _] {:status 403 :body "You are not authorized."})})))
     {:handler {:and [authenticated-user]}}))

  (context "/webhooks" []
    (POST "/plaid" [] plaid/hook))

  ;; catch-all
  (route/not-found "<p>Not Found</p>"))
