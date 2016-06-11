(ns starcity.pages.auth
  (:require [starcity.pages.auth.login :as login]
            [starcity.pages.auth.signup :as signup]
            [starcity.pages.util :refer [ok]]
            [starcity.router :refer [route]]
            [starcity.util :refer :all]
            [buddy.auth :refer [authenticated?]]
            [ring.util.response :as response]))

;; =============================================================================
;; API

;; (defmethod route :signup [_ {:keys [request-method] :as req}]
;;   (if (authenticated? req)
;;     (response/redirect login/+redirect-after-login+) ; TODO: Better system
;;     (case request-method
;;       :get (ok (signup/render-signup req))
;;       :post (signup/signup req)
;;       (ok (signup/render-signup req)))))

;; (defmethod route :login [_ req]
;;   (if (authenticated? req)
;;     (response/redirect login/+redirect-after-login+)
;;     (case (:request-method req)
;;       :get  (ok (login/render req))
;;       :post (login/authenticate req)
;;       (ok (login/render req)))))


;; (defmethod route :signup/activate [_ {:keys [params] :as req}]
;;   (let [{:keys [email hash]} params]
;;     (cond
;;       (authenticated? req) (response/redirect login/+redirect-after-login+)
;;       (and email hash)     (signup/activate req)
;;       :otherwise           (signup/render-invalid-activation req))))

(defmethod route [:logout :get] [_ _]
  (-> (response/redirect "/login")
      (assoc :session {})))
