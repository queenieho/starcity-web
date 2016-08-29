(ns starcity.views.onboarding
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-js include-css]]
            [cheshire.core :as json]
            [starcity.config :refer [config]]))

(defn base
  [req]
  (html5
   {:lang "en"}
   [:head
    [:link {:href "https://fonts.googleapis.com/css?family=Lato:300,400,700&subset=latin-ext" :rel "stylesheet" :type "text/css"}]
    [:link {:href "https://fonts.googleapis.com/icon?family=Material+Icons" :rel "stylesheet"}]
    (include-css "/assets/css/app.css")
    ;; Let browser know website is optimized for mobile
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]]
   [:body {:style "background-color: #fbf8f6;"}
    [:script
     (format "var plaid = %s" (json/encode {:key (get-in config [:plaid :public-key])
                                          :env (get-in config [:plaid :env])}))]
    (include-js "https://cdn.plaid.com/link/stable/link-initialize.js"
                "https://js.stripe.com/v2/"
                "/js/elm/onboarding.js"
                "/js/elm/onboarding/onboarding_bootstrap.js")]))
