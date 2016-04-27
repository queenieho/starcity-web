(ns starcity.views.landing
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-css include-js]]))

;; =============================================================================
;; Constants

;; (def ^{:private true} HEAD-JS
;;   [])

(def ^{:private true} HEAD-CSS
  ["assets/css/bootstrap.css"
   "assets/css/material-design-iconic-font.min.css"
   "assets/css/re-com.css"])

(def ^{:private true} HEAD-FONTS
  ["http://fonts.googleapis.com/css?family=Roboto:300,400,500,700,400italic"
   "http://fonts.googleapis.com/css?family=Roboto+Condensed:400,300"])

(def ^{:private true} BODY-JS
  ["/js/main.js"])

;; =============================================================================
;; Components

(defn- head [title]
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge"}]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
   (apply include-css (concat HEAD-CSS HEAD-FONTS))
   [:title title]])

;; =============================================================================
;; API

(defn landing-page [req]
  (html5
   {:lang "en"}
   (head "Starcity")
   [:body
    [:div#app
     [:h1 "Hello, Starcity!"]]
    ;; TODO: advanced cljs compilation
    ;; (apply include-js BODY-JS)
    ]))
