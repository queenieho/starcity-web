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
             [apply :as apply]
             [account :as account]
             [auth :as auth]
             [login :as login]
             [signup :as signup]
             [communities :as communities]
             [faq :as faq]
             [landing :as landing]
             [terms :as terms]
             [privacy :as privacy]
             [team :as team]
             [about :as about]
             [onboarding :as onboarding]
             [admin :as admin]
             [dashboard :as dashboard]]))

(defn- redirect-by-role
  [{:keys [identity] :as req} msg]
  (-> (condp = (:account/role identity)
        :account.role/applicant "/apply"
        :account.role/pending   "/onboarding"
        :account.role/admin     "/admin"
        "/")
      (response/redirect)))

;; =============================================================================
;; API
;; =============================================================================

(defroutes app-routes
  (GET "/"                 [] landing/show-landing)
  (POST "/"                [] landing/newsletter-signup)

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

  (context "/apply" []
    (restrict
        (routes
         (GET "*" [] apply/show-apply))
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
    (restrict onboarding/routes
      {:handler  {:and [authenticated-user (user-isa :account.role/pending)]}
       :on-error redirect-by-role}))

  (context "/api/v1" [] api/routes)

  (context "/webhooks" []
    (POST "/plaid" [] plaid/hook)
    (POST "/stripe" [] stripe/hook))

  ;; catch-all
  (route/not-found "<p>Not Found</p>"))
