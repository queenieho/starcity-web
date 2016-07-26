(ns starcity.views.elm
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-js]]))

(defn base
  []
  (html5
   {:lang "en"}
   [:head
    [:link {:href "https://fonts.googleapis.com/css?family=Roboto:400,300,500|Roboto+Mono|Roboto+Condensed:400,700&subset=latin,latin-ext" :rel "stylesheet" :type "text/css"}]
    [:link {:href "https://fonts.googleapis.com/icon?family=Material+Icons" :rel "stylesheet"}]
    [:link {:type  "text/css"
            :rel   "stylesheet"
            :href  "https://code.getmdl.io/1.1.3/material.indigo-pink.min.css"
            :media "screen,projection"}]

    ;; Let browser know whebsite is optimized for mobile
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]]
   [:body
    (include-js "/js/elm.js")
    [:script
     "var app = Elm.Main.fullscreen();"]]))
