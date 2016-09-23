(ns starcity.routes
  (:require [buddy.auth :refer [authenticated?]]
            [buddy.auth.accessrules :refer [restrict]]
            [compojure
             [core :refer [ANY context defroutes GET POST routes]]
             [route :as route]]
            [ring.util.response :as response]
            [starcity.auth :refer [authenticated-user user-isa]]
            [starcity.api :as api]
            [starcity.webhooks
             [plaid :as plaid]
             [stripe :as stripe]]
            [starcity.controllers
             [account :as account]
             [application :as application]
             [auth :as auth]
             [communities :as communities]
             [faq :as faq]
             [landing :as landing]
             [register :as register]
             [terms :as terms]
             [privacy :as privacy]
             [team :as team]
             [about :as about]
             [onboarding :as onboarding]
             [admin :as admin]
             [dashboard :as dashboard]]
            [starcity.controllers.application
             [personal :as personal]
             [logistics :as logistics]
             [community-fitness :as community-fitness]
             [submit :as submit]]
            [starcity.controllers.auth
             [login :as login]
             [signup :as signup]]))

(defn- redirect-by-role
  [{:keys [identity] :as req} msg]
  (-> (condp = (:account/role identity)
        :account.role/applicant "/application"
        :account.role/pending   "/onboarding"
        :account.role/admin     "/admin"
        "/")
      (response/redirect)))

;; =============================================================================
;; API
;; =============================================================================

(defroutes app-routes
  ;; public
  (GET "/"                 [] landing/show-landing)
  (GET "/register"         [] register/register-user!)
  (GET "/communities"      [] communities/show-communities)
  (GET "/faq"              [] faq/show-faq)
  (GET "/terms"            [] terms/show-terms)
  (GET "/privacy"          [] privacy/show-privacy)
  (GET "/about"            [] about/show-about)
  (GET "/team"             [] team/show-team)

  (GET "/forgot-password"  [] auth/show-forgot-password)
  (POST "/forgot-password" [] auth/forgot-password!)

  (GET  "/login"           [] login/show-login)
  (POST "/login"           [] login/login!)

  (ANY  "/logout"       [] auth/logout!)

  ;; New Application
  (GET "/apply" [] (restrict application/show-apply
                             {:handler  {:and [authenticated-user (user-isa :account.role/applicant)]}
                              :on-error redirect-by-role}))

  (context "/account" []
    (restrict
        (routes
         (GET "/"          [] account/show-account-settings)
         (POST "/password" [] account/update-password!))
      {:handler  authenticated-user
       :on-error redirect-by-role}))

  (context "/account" []
    (restrict
        (routes
         (GET "/"          [] account/show-account-settings)
         (POST "/password" [] account/update-password!))
      {:handler  authenticated-user
       :on-error redirect-by-role}))

  (context "/signup" []
    (GET   "/"         [] signup/show-signup)
    (POST  "/"         [] signup/signup!)
    (GET   "/complete" [] signup/show-complete)
    (GET   "/activate" [] signup/activate!))

  ;; auth
  (context "/application"         []
    (restrict
        (routes
         (GET "/"             [] application/show-application)

         (restrict
          (routes
           (GET "/logistics"  [] logistics/show-logistics)
           (POST "/logistics" [] logistics/save!))
          logistics/restrictions)

         (restrict
          (routes
           (GET "/personal"   [] personal/show-personal)
           (POST "/personal"  [] personal/save!))
          personal/restrictions)

         (restrict
          (routes
           (GET "/community"  [] community-fitness/show-community-fitness)
           (POST "/community" [] community-fitness/save!))
          community-fitness/restrictions)

         (restrict
          (routes
           (GET "/submit"     [] submit/show-submit)
           (POST "/submit"    [] submit/submit!))
          submit/restrictions))

      {:handler  {:and [authenticated-user (user-isa :account.role/applicant)]}
       :on-error redirect-by-role}))

  (context "/admin" []
    (restrict
        (routes
         (GET "*" [] admin/show))
      {:handler  {:and [authenticated-user (user-isa :account.role/admin)]}
       :on-error redirect-by-role}))

  (context "/me" []
    (restrict
        (routes
         (GET "/*" [] dashboard/show))
      {:handler  {:and [authenticated-user (user-isa :account.role/tenant)]}
       :on-error redirect-by-role}))


  (context "/onboarding" []
    (restrict
        onboarding/routes
      {:handler  {:and [authenticated-user (user-isa :account.role/pending)]}
       :on-error redirect-by-role}))

  (context "/api/v1" []
    (restrict api/routes {:handler authenticated-user}))

  (context "/webhooks" []
    (POST "/plaid" [] plaid/hook)
    (POST "/stripe" [] stripe/hook))

  ;; catch-all
  (route/not-found "<p>Not Found</p>"))
