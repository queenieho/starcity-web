(ns starcity.routes
  (:require [accountant.core :as accountant]
            [re-frame.core :refer [dispatch]]
            [secretary.core :as secretary])
  (:require-macros [secretary.core :refer [defroute]]))

;; NOTE: See https://gist.github.com/city41/aab464ae6c112acecfe1
;; NOTE: See http://www.lispcast.com/mastering-client-side-routing-with-secretary-and-goog-history

(def APP-ROOT-URI "/apply")

(defn navigate! [route]
  (accountant/navigate! (str APP-ROOT-URI route)))

(defn hook-browser-navigation! []
  (accountant/configure-navigation!
   {:nav-handler  #(secretary/dispatch! %)
    :path-exists? #(secretary/locate-route %)}))

(defn app-routes []
  (secretary/reset-routes!)             ; for dev purposes...doesn't seem to be working

  (defroute home "/apply" []
    (dispatch [:app/nav :home]))

  (defroute basic "/apply/basic" []
    (dispatch [:app/nav :basic]))

  (defroute residence "/apply/residence" []
    (dispatch [:app/nav :residence]))

  (defroute "/apply/*" []
    (accountant/navigate! APP-ROOT-URI))

  ;; --------------------

  (hook-browser-navigation!)

  (accountant/dispatch-current!))
