(ns admin.routes
  (:require [accountant.core :as accountant]
            [re-frame.core :refer [dispatch]]
            [secretary.core :as secretary])
  (:require-macros [secretary.core :refer [defroute]]))

;; NOTE: See https://gist.github.com/city41/aab464ae6c112acecfe1
;; NOTE: See http://www.lispcast.com/mastering-client-side-routing-with-secretary-and-goog-history

;; =============================================================================
;; Constants

(def ^:private root "/admin")

;; =============================================================================
;; Helpers

(defn- prefix [uri]
  (str root uri))

;; =============================================================================
;; API

(defn build-path
  ([] root)
  ([uri] (prefix (str "/" uri))))

;; TODO: Ensure it's from the app root URI
(defn navigate! [route]
  (accountant/navigate! route))

(defn hook-browser-navigation! []
  (accountant/configure-navigation!
   {:nav-handler  #(secretary/dispatch! %)
    :path-exists? #(secretary/locate-route %)}))

(defn app-routes []

  (defroute home root []
    (dispatch [:nav/home]))

  (defroute applications (prefix "/applications") []
    (dispatch [:nav/applications]))

  (defroute application (prefix "/applications/:id") [id]
    (dispatch [:nav/application (js/parseInt id)]))

  (defroute accounts (prefix "/accounts") []
    (dispatch [:nav/accounts]))

  (defroute account (prefix "/accounts/:id") [id]
    (dispatch [:nav/account (js/parseInt id) :overview]))

  (defroute account-section (prefix "/accounts/:id/:section") [id section]
    (dispatch [:nav/account (js/parseInt id) (keyword section)]))

  (defroute (prefix "/*") []
    (accountant/navigate! root))

  ;; --------------------

  (hook-browser-navigation!)

  (accountant/dispatch-current!))
