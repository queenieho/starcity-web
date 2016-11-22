(ns starcity.routes
  (:require [buddy.auth.accessrules :refer [restrict]]
            [compojure
             [core :refer [ANY context defroutes GET POST routes]]
             [route :as route]]
            [ring.util.response :as response]
            [starcity
             [api :as api]
             [auth :refer [authenticated-user user-isa]]]
            [starcity.controllers
             [about :as about]
             [admin :as admin]
             [apply :as apply]
             [auth :as auth]
             [communities :as communities]
             [dashboard :as dashboard]
             [faq :as faq]
             [landing :as landing]
             [login :as login]
             [onboarding :as onboarding]
             [privacy :as privacy]
             [settings :as settings]
             [signup :as signup]
             [team :as team]
             [terms :as terms]]
            [starcity.webhooks
             [plaid :as plaid]
             [stripe :as stripe]]))

(defn- redirect-by-role
  [{:keys [identity] :as req} msg]
  (-> (case (:account/role identity)
        :account.role/applicant  "/apply"
        :account.role/onboarding "/onboarding"
        :account.role/admin      "/admin"
        :account.role/member     "/me"
        "/")
      (response/redirect)))

;; =============================================================================
;; API
;; =============================================================================

(defroutes app-routes
  (GET "/"                 [] landing/show-landing)
  (POST "/"                [] landing/newsletter-signup)

  (GET "/faq"              [] faq/show-faq)
  (GET "/terms"            [] terms/show-terms)
  (GET "/privacy"          [] privacy/show-privacy)
  (GET "/about"            [] about/show-about)
  (GET "/team"             [] team/show-team)

  (GET "/forgot-password"  [] auth/show-forgot-password)
  (POST "/forgot-password" [] auth/forgot-password)

  (GET  "/login"           [] login/show-login)
  (POST "/login"           [] login/login)

  (ANY  "/logout"          [] auth/logout)

  (context "/communities" []
           (GET "/" [] communities/show-communities)
           (GET "/soma" [] communities/show-soma)
           (GET "/mission" [] communities/show-mission))

  (context "/signup" []
           (GET   "/"         [] signup/show-signup)
           (POST  "/"         [] signup/signup)
           (GET   "/complete" [] signup/show-complete)
           (GET   "/activate" [] signup/activate))

  (context "/apply" []
           (restrict
            (routes
             (GET "*" [] apply/show-apply))
            {:handler  {:and [authenticated-user (user-isa :account.role/applicant)]}
             :on-error redirect-by-role}))

  (context "/settings" []
           (restrict
            (routes
             (GET "/"          []
                  (fn [_] (ring.util.response/redirect "/settings/change-password")))
             (GET "/change-password"  [] settings/show-account-settings)
             (POST "/change-password" [] settings/update-password))
            {:handler  authenticated-user
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
            {:handler  {:and [authenticated-user (user-isa :account.role/member)]}
             :on-error redirect-by-role}))


  (context "/onboarding" []
           (restrict onboarding/routes
                     {:handler  {:and [authenticated-user (user-isa :account.role/onboarding)]}
                      :on-error redirect-by-role}))

  (context "/api/v1" [] api/routes)

  (context "/webhooks" []
           (POST "/plaid" [] plaid/hook)
           (POST "/stripe" [] stripe/hook))

  ;; catch-all
  (route/not-found "<p>Not Found</p>"))
