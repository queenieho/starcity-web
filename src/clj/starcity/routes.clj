(ns starcity.routes
  (:require [buddy.auth.accessrules :refer [restrict]]
            [compojure
             [core :refer [ANY context defroutes GET POST routes]]
             [route :as route]]
            [customs.access :as access]
            [ring.util.response :as response]
            [starcity.api :as api]
            [starcity.controllers
             [admin :as admin]
             [apply :as apply]
             [auth :as auth]
             [careers :as careers]
             [collaborate :as collaborate]
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
             [signup :as signup]
             [story :as story]
             [terms :as terms]]
            [starcity.webhooks.stripe :as stripe]))

;; =============================================================================
;; Routes
;; =============================================================================

(defn redirect-by-role
  "Redirect to the appropriate URI based on logged-in user's role."
  [{:keys [identity] :as req}]
  (-> (case (:account/role identity)
        :account.role/applicant  "/apply"
        :account.role/onboarding "/onboarding"
        :account.role/admin      "/admin"
        :account.role/member     "/me"
        "/")
      (response/redirect)))

(defroutes app-routes
  (GET  "/"                [] landing/show)

  (GET "/lifestyle"        [] lifestyle/show)
  (GET "/story"            [] story/show)
  (GET "/careers"          [] careers/show)
  (GET "/faq"              [] faq/show)

  (GET "/schedule-tour"    [] schedule-tour/show)
  (POST "/schedule-tour"   [] schedule-tour/submit!)

  (GET "/collaborate"      [] collaborate/show)
  (POST "/collaborate"     [] collaborate/submit!)

  (GET "/terms"            [] terms/show)
  (GET "/privacy"          [] privacy/show)

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
            {:handler  access/unauthenticated-user
             :on-error (fn [req _] (redirect-by-role req))}))

  (ANY  "/logout"          [] auth/logout)

  (context "/signup" []
           (restrict
            (routes
             (GET   "/"         [] signup/show-signup)
             (POST  "/"         [] signup/signup)
             (GET   "/complete" [] signup/show-complete)
             (GET   "/activate" [] signup/activate))
            {:handler  access/unauthenticated-user
             :on-error (fn [req _] (redirect-by-role req))}))

  (context "/apply" []
           (restrict
            (routes
             (GET "*" [] apply/show))
            {:handler  {:and [access/authenticated-user (access/user-isa :account.role/applicant)]}
             :on-error (fn [req _] (redirect-by-role req))}))

  ;; TODO: Disabling until I have an idea of how to better accomplish this.
  #_(context "/settings" []
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
            {:handler  {:and [access/authenticated-user (access/user-isa :account.role/admin)]}
             :on-error (fn [req _] (redirect-by-role req))}))

  (context "/me" []
           (restrict
            (routes
             (GET "/*" [] dashboard/show))
            {:handler  {:and [access/authenticated-user (access/user-isa :account.role/member)]}
             :on-error (fn [req _] (redirect-by-role req))}))


  (context "/onboarding" []
           (restrict
            (routes
             (GET "*" [] onboarding/show))
            {:handler  {:and [access/authenticated-user (access/user-isa :account.role/onboarding)]}
             :on-error (fn [req _] (redirect-by-role req))})

           #_(restrict onboarding/routes
                       {:handler  {:and [authenticated-user (user-isa :account.role/onboarding)]}
                        :on-error redirect-by-role}))

  (context "/api/v1" [] api/routes)

  (context "/webhooks" []
           (POST "/stripe" [] stripe/hook))

  ;; catch-all
  (route/not-found "<p>Not Found</p>"))
