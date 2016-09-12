(ns starcity.views.admin
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-js]]))

(defn base
  []
  (html5
   {:lang "en"}
   [:head
    ;; [:link {:href "https://fonts.googleapis.com/css?family=Roboto:400,300,500|Roboto+Mono|Roboto+Condensed:400,700&subset=latin,latin-ext" :rel "stylesheet" :type "text/css"}]
    ;; [:link {:href "https://fonts.googleapis.com/icon?family=Material+Icons" :rel "stylesheet"}]
    [:link {:type  "text/css"
            :rel   "stylesheet"
            :href  "/assets/css/starcity.css"
            :media "screen,projection"}]

    ;; Let browser know website is optimized for mobile
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]]
   [:body
    [:div#app]
    (include-js "/js/cljs/admin.js")
    [:script "window.onload = function() { admin.core.run(); }"]]))
