(ns starcity.routes
  (:require [buddy.auth.accessrules :refer [restrict]]
            [compojure
             [core :refer [ANY context defroutes GET POST routes]]
             [route :as route]]
            [ring.util.response :as response]
            [starcity
             [api :as api]
             [auth :refer [authenticated-user redirect-by-role unauthenticated-user user-isa]]]
            [starcity.controllers
             [about :as about]
             [admin :as admin]
             [apply :as apply]
             [auth :as auth]
             [communities :as communities]
             [dashboard :as dashboard]
             [faq :as faq]
             [landing :as landing]
             [lifestyle :as lifestyle]
             [login :as login]
             [newsletter :as newsletter]
             [onboarding :as onboarding]
             [privacy :as privacy]
             [schedule-tour :as schedule-tour]
             [settings :as settings]
             [signup :as signup]
             [story :as story]
             [team :as team]
             [terms :as terms]]
            [starcity.webhooks.stripe :as stripe]))

;; =============================================================================
;; Routes
;; =============================================================================

(defroutes app-routes
  (GET  "/"                [] landing/show)

  (GET "/lifestyle"        [] lifestyle/show)
  (GET "/story"            [] story/show)
  (GET "/schedule-tour"    [] schedule-tour/show)

  (GET "/faq"              [] faq/show)
  (GET "/terms"            [] terms/show)
  (GET "/privacy"          [] privacy/show)
  (GET "/about"            [] about/show-about)
  (GET "/team"             [] team/show-team)
  (GET "/newsletter"       [] newsletter/show)
  (POST "/newsletter"      [] newsletter/subscribe!)

  (context "/communities" []
           (GET "/soma" [] communities/show-soma)
           (GET "/mission" [] communities/show-mission))

  (GET "/forgot-password"  [] auth/show-forgot-password)
  (POST "/forgot-password" [] auth/forgot-password)

  (context "/login" []
           (restrict
            (routes
             (GET  "/"           [] login/show)
             (POST "/"           [] login/login))
            {:handler  unauthenticated-user
             :on-error (fn [req _] (redirect-by-role req))}))

  (ANY  "/logout"          [] auth/logout)

  (context "/signup" []
           (restrict
            (routes
             (GET   "/"         [] signup/show-signup)
             (POST  "/"         [] signup/signup)
             (GET   "/complete" [] signup/show-complete)
             (GET   "/activate" [] signup/activate))
            {:handler  unauthenticated-user
             :on-error (fn [req _] (redirect-by-role req))}))

  (context "/apply" []
           (restrict
            (routes
             (GET "*" [] apply/show-apply))
            {:handler  {:and [authenticated-user (user-isa :account.role/applicant)]}
             :on-error (fn [req _] (redirect-by-role req))}))

  (context "/settings" []
           (restrict
            (routes
             (GET "/"          []
                  (fn [_] (ring.util.response/redirect "/settings/change-password")))
             (GET "/change-password"  [] settings/show-account-settings)
             (POST "/change-password" [] settings/update-password))
            {:handler  authenticated-user
             :on-error (fn [req _] (redirect-by-role req))}))

  (context "/admin" []
           (restrict
            (routes
             (GET "*" [] admin/show))
            {:handler  {:and [authenticated-user (user-isa :account.role/admin)]}
             :on-error (fn [req _] (redirect-by-role req))}))

  (context "/me" []
           (restrict
            (routes
             (GET "/*" [] dashboard/show))
            {:handler  {:and [authenticated-user (user-isa :account.role/member)]}
             :on-error (fn [req _] (redirect-by-role req))}))


  (context "/onboarding" []
           (restrict onboarding/routes
                     {:handler  {:and [authenticated-user (user-isa :account.role/onboarding)]}
                      :on-error (fn [req _] (redirect-by-role req))}))

  (context "/api/v1" [] api/routes)

  (context "/webhooks" []
           (POST "/stripe" [] stripe/hook))

  ;; catch-all
  (route/not-found "<p>Not Found</p>"))
