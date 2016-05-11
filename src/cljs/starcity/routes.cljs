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
   {:nav-handler #(secretary/dispatch! %)
    :path-exists? #(secretary/locate-route %)}))

(defn app-routes []
  (defroute "/apply" []
    (dispatch [:app/nav :home]))

  (defroute "/apply/test1" []
    (dispatch [:app/nav :about]))


  ;; --------------------
  (hook-browser-navigation!)

  (accountant/dispatch-current!))
