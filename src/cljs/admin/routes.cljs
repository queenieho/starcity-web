(ns admin.routes
  (:require [accountant.core :as accountant]
            [re-frame.core :refer [dispatch]]
            [secretary.core :as secretary]
            [starcity.utils :refer [transform-when-key-exists]]
            [starcity.log :as l])
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

(defn navigate! [route]
  (accountant/navigate! route))

(defn hook-browser-navigation! []
  (accountant/configure-navigation!
   {:nav-handler  #(secretary/dispatch! %)
    :path-exists? #(secretary/locate-route %)}))

(defn- transform-table-query-params [params]
  (transform-when-key-exists params
    {:limit     js/parseInt
     :offset    js/parseInt
     :sort-key  keyword
     :direction keyword
     :view      keyword}))

(defn app-routes []

  (defroute home root []
    (dispatch [:nav/home]))

  (defroute applications (prefix "/applications") [query-params]
    (dispatch [:nav/applications (transform-table-query-params query-params)]))

  (defroute application (prefix "/applications/:id") [id]
    (dispatch [:nav/application (js/parseInt id)]))

  (defroute accounts (prefix "/accounts") [query-params]
    (dispatch [:nav/accounts (transform-table-query-params query-params)]))

  (defroute account (prefix "/accounts/:id") [id]
    (dispatch [:nav/account (js/parseInt id) :overview]))

  (defroute account-section (prefix "/accounts/:id/:section") [id section]
    (dispatch [:nav/account (js/parseInt id) (keyword section)]))

  (defroute (prefix "/*") []
    (accountant/navigate! root))

  ;; --------------------

  (hook-browser-navigation!)

  (accountant/dispatch-current!))
