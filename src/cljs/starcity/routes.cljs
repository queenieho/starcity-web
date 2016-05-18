(ns starcity.routes
  (:require [accountant.core :as accountant]
            [re-frame.core :refer [dispatch]]
            [secretary.core :as secretary])
  (:require-macros [secretary.core :refer [defroute]]))

;; NOTE: See https://gist.github.com/city41/aab464ae6c112acecfe1
;; NOTE: See http://www.lispcast.com/mastering-client-side-routing-with-secretary-and-goog-history

;; =============================================================================
;; Constants

(def ^:private ROOT "/me")

;; =============================================================================
;; Helpers

(defn- prefix [uri]
  (str ROOT uri))

(defn- phase-id->location
  [phase-id]
  (get {2 :phase/two} phase-id :unknown))

;; =============================================================================
;; API

;; TODO: Ensure it's form the app root URI
(defn navigate! [route]
  (accountant/navigate! route))

(defn hook-browser-navigation! []
  (accountant/configure-navigation!
   {:nav-handler  #(secretary/dispatch! %)
    :path-exists? #(secretary/locate-route %)}))

(defn app-routes []

  (defroute home ROOT []
    (dispatch [:app/nav :home {}]))

  (defroute phase (prefix "/phase/:phase-id/:section/:group") {:as params}
    (let [{:keys [phase-id] :as params} (update params :phase-id js/parseInt)]
      (dispatch [:app/nav (phase-id->location phase-id) params])))

  (defroute (prefix "/*") []
    (accountant/navigate! ROOT))

  ;; --------------------

  (hook-browser-navigation!)

  (accountant/dispatch-current!))
