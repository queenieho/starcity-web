(ns starcity.auth
  (:require [ring.util.response :as response]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [buddy.auth.backends.session :refer [session-backend]]
            [buddy.auth.accessrules :refer [success error]]
            [datomic.api :as d]
            [starcity.datomic :refer [conn]]))

;; =============================================================================
;; Constants
;; =============================================================================

(def ^:private permissions {})

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- unauthorized-handler
  "Default unauthorized handler."
  [{:keys [headers] :as request} metadata]
  (cond
    (authenticated? request) (-> (response/response "You are not authorized to view this page.")
                                 (response/content-type "text/html; charset=utf-8")
                                 (assoc :status 403))
    :else                    (let [current-url (:uri request)]
                               ;; NOTE: Treat /application as a special case,
                               ;; since it'll be triggered from the landing page
                               ;; most frequently
                               (if (= current-url "/application")
                                 (response/redirect "/signup")
                                 (response/redirect (format "/login?next=%s" current-url))))))

;; =============================================================================
;; API
;; =============================================================================

(def auth-backend
  (session-backend {:unauthorized-handler unauthorized-handler}))

(defn requester
  "Produces the entity of the user that made the request."
  [req]
  (let [account-id (get-in req [:identity :db/id])]
    (d/entity (d/db conn) account-id)))

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

;; =============================================================================
;; Access Rules

(defn- get-role [req]
  (let [account (requester req)]
    (:account/role account)))

(defn authenticated-user [req]
  (if (authenticated? req)
    true
    (throw-unauthorized)))

(defn unauthenticated-user [req]
  (not (authenticated? req)))

(defn user-can
  "Given a particular action that the authenticated user desires to perform,
  return a handler that determines if their user level is authorized to perform
  that action."
  [action]
  (fn [req]
    (let [user-role      (get-role req)
          required-roles (get permissions action #{})]
      (if (some #(isa? user-role %) required-roles)
        (success)
        (error (format "User with role %s is not authorized for action %s"
                       (name user-role) (name action)))))))

(defn user-isa
  "Return a handler that determines whether the authenticated user is of a
  specific role OR any derived role."
  [role]
  (fn [req]
    (if (isa? (get-role req) role)
      (success)
      (error (format "User is not a(n) %s" (name role))))))

(defn user-is
  "Return a handler that determines whether the authenticated user is of a
  specific role."
  [role]
  (fn [req]
    (if (= (get-role req) role)
      (success)
      (error (format "User is not a(n) %s" (name role))))))

(defn user-has-id
  "Return a handler that determines whether the authenticated user has a given ID.
  This is useful, for example, to determine if the user is the owner of the
  requested resource."
  [id]
  (fn [req]
    (if (= id (get-in req [:identity :db/id]))
      (success)
      (error (str "User does not have id given %s" id)))))
