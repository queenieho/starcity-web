(ns mars.routes
  (:require [accountant.core :as accountant]
            [re-frame.core :refer [dispatch]]
            [secretary.core :as secretary]
            [starcity.utils :refer [transform-when-key-exists]]
            [starcity.log :as l])
  (:require-macros [secretary.core :refer [defroute]]))

;; =============================================================================
;; Constants

(def ^:private root "/me")

;; =============================================================================
;; Helpers

(defn- prefix [uri]
  (str root uri))

;; =============================================================================
;; API

(defn build-path
  ([] root)
  ([uri] (prefix (str "/" uri))))

(defn navigate! [route]
  (accountant/navigate! route))

(defn hook-browser-navigation! []
  (accountant/configure-navigation!
   {:nav-handler  #(secretary/dispatch! %)
    :path-exists? #(secretary/locate-route %)}))

(defroute activity root []
  (dispatch [:nav/activity]))

(defroute account (prefix "/account/:subsection") [subsection]
  (dispatch [:nav/account subsection]))

(defroute (prefix "/*") []
  (accountant/navigate! root))


(defn app-routes []

  ;; --------------------

  (hook-browser-navigation!)

  (accountant/dispatch-current!))
