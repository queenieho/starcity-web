(ns starcity.auth
  (:require [starcity.views.error :as view]
            [ring.util.response :as response]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [buddy.auth.backends.session :refer [session-backend]]
            [buddy.auth.accessrules :refer [success error]]))

;; TODO: Different uses depending on whether or not this is an API req or Page
;; req
(defn unauthorized-handler
  [request metadata]
  (cond
    (authenticated? request) (-> (view/error "You are not authorized to view this page.")
                                 (response/response)
                                 (response/content-type "text/html; charset=utf-8")
                                 (assoc :status 403))
    :else                    (let [current-url (:uri request)]
                               (response/redirect (format "/login?next=%s" current-url)))))

(def auth-backend
  (session-backend {:unauthorized-handler unauthorized-handler}))

;; TODO: Add some permissions
(def permissions {})

(defn authenticated-user [req]
  (if (authenticated? req)
    true
    (throw-unauthorized)))

(defn- get-role [req]
  (get-in req [:identity :account/role]))

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

;; TODO: The error message thing is a little cheesy
(defn user-passes
  ([predicate]
   (user-passes predicate "Didn't pass predicate!"))
  ([predicate error-msg]
   (fn [req]
     (if (predicate req)
       (success)
       (error error-msg)))))

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
