(ns starcity.pages.auth
  (:require [starcity.pages.auth.login :as login]
            [starcity.pages.auth.signup :as signup]
            [starcity.pages.util :refer [ok]]
            [starcity.util :refer :all]
            [buddy.auth :refer [authenticated?]]
            [ring.util.response :as response]))

;; =============================================================================
;; API

(defn handle-signup [{:keys [request-method] :as req}]
  (if (authenticated? req)
    (response/redirect login/+redirect-after-login+) ; TODO: Better system
    (case request-method
      :get (ok (signup/render req))
      :post (signup/signup req)
      (ok (signup/render req)))))

(defn handle-signup-complete [req]
  (ok (signup/render-complete req)))

(defn handle-login [req]
  (if (authenticated? req)
    (response/redirect login/+redirect-after-login+)
    (case (:request-method req)
      :get  (ok (login/render req))
      :post (login/authenticate req)
      (ok (login/render req)))))

(defn handle-logout [req]
  (-> (response/redirect "/login")
      (assoc :session {})))
