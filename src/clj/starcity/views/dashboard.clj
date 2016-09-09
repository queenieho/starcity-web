(ns starcity.views.dashboard
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-js include-css]]))

(defn base
  [req]
  (html5
   {:lang "en"}
   [:head
    [:link {:href "https://fonts.googleapis.com/css?family=Roboto:400,300,500|Roboto+Mono|Roboto+Condensed:400,700&subset=latin,latin-ext" :rel "stylesheet" :type "text/css"}]
    [:link {:href "https://fonts.googleapis.com/icon?family=Material+Icons" :rel "stylesheet"}]
    (include-css "/assets/css/app.css"
                 "/assets/css/mdl-stepper.min.css")

    ;; Let browser know whebsite is optimized for mobile
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]]
   [:body
    (include-js "/js/elm/dashboard.js")
    [:script "var app = Elm.Dashboard.fullscreen();"]]))
