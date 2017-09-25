(ns starcity.routes
  (:require [buddy.auth.accessrules :refer [restrict]]
            [compojure
             [core :refer [ANY context defroutes GET POST ANY routes]]
             [route :as route]]
            [customs.access :as access]
            [ring.util.response :as response]
            [starcity.api :as api]
            [starcity.config :as config :refer [config]]
            [starcity.controllers
             [admin :as admin]
             [auth :as auth]
             [careers :as careers]
             [collaborate :as collaborate]
             [communities :as communities]
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
        :account.role/applicant  (config/apply-hostname config)
        :account.role/onboarding "/onboarding"
        :account.role/admin      "/admin"
        :account.role/member     (config/odin-hostname config)
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
    (GET "/mission" [] communities/show-mission)
    (GET "/coming-soon" [] communities/show-coming-soon)
    (POST "/coming-soon" [] communities/submit-suggestions!))

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

  (context "/admin" []
    (restrict
        (routes
         (GET "*" [] admin/show))
      {:handler  {:and [access/authenticated-user (access/user-isa :account.role/admin)]}
       :on-error (fn [req _] (redirect-by-role req))}))

  (ANY "/me" [] (fn [_]
                  (response/redirect (config/odin-hostname config))))


  (context "/onboarding" []
    (restrict
        (routes
         (GET "*" [] onboarding/show))
      {:handler  {:and [access/authenticated-user (access/user-isa :account.role/onboarding)]}
       :on-error (fn [req _] (redirect-by-role req))}))

  (GET "/apply" []
       (fn [_]
         (response/redirect (config/apply-hostname config) :moved-permanently)))

  (context "/api/v1" [] api/routes)

  (context "/webhooks" []
    (POST "/stripe" [] stripe/hook))

  ;; catch-all
  (route/not-found "<p>Not Found</p>"))
