(ns starcity.pages.register
  (:require [starcity.pages.base :refer [base]]
            [starcity.pages.util :refer [ok]]
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

(def navbar
  [:nav.navbar
   [:div.container
    [:div.navbar-header
     [:a.navbar-brand {:href "#"}
      [:img {:alt "Starcity" :src "/assets/img/starcity-brand-icon-white.png"}]]]]])

(defn- content [{:keys [params] :as req}]
  [:div.navbar-wrapper
   [:div.container-fluid
    navbar

    [:div.container
     [:h3 (format "Thank you for getting involved, %s!" (:email params)) " With your help, we'll design and build community-focused housing that will keep San Francisco's diversity right here, in our city. <br> <br> Let's work together to make San Francisco a financially attainable place to live by adding new housing supply to our city. Be on the lookout for an email with more information on how to participate."]]]])

(defn- view [req]
  (base (content req)
        :css ["register.css"]            ; TODO:
        ))

;; =============================================================================
;; API

(defn handle [{:keys [params] :as req}]
  (if-let [email (:email params)]
    (do
      (mailchimp/add-interested-subscriber! email (partial log-subscriber-request email))
      (ok (view req)))
    (ok (view req))))
