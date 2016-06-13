(ns starcity.pages.register
  (:require [starcity.pages.base :refer [base]]
            [starcity.pages.util :refer [ok]]
            [starcity.router :refer [route]]
            [starcity.services.mailchimp :as mailchimp]
            [ring.util.response :as response]
            [taoensso.timbre :refer [debug infof]]))

;; =============================================================================
;; Helpers

(defn- log-subscriber-request
  [email {:keys [status body]}]
  (infof "MAILCHIMP :: add subscriber :: email - %s :: status - %s :: body - %s"
         email status body))

;; =============================================================================
;; Components

(defn- content [{:keys [params] :as req}]
  [:h3 (format "Prepare for spam, %s!" (:email params))])

(defn- view [req]
  (base (content req)
        :css ["landing.css"]            ; TODO:
        ))

;; =============================================================================
;; API

(defn register-user [{:keys [params] :as req}]
  (when-let [email (:email params)]
    (mailchimp/add-interested-subscriber! email (partial log-subscriber-request email)))
  (ok (view req)))

;; (defmethod route [:register :get] [_ {:keys [params] :as req}]
;;   (if-let [email (:email params)]
;;     (do
;;       (mailchimp/add-interested-subscriber! email (partial log-subscriber-request email))
;;       (ok (view req)))
;;     (ok (view req))))
