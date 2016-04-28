(ns starcity.pages.base
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-css include-js]]))

;;; =============================================================================
;; Constants

;; (def ^{:private true} HEAD-JS
;;   [])

(def BASE-CSS-DIR "assets/css")

(defn- css-path [filename]
  (format "%s/%s" BASE-CSS-DIR filename))

(def ^{:private true} HEAD-CSS
  (map css-path
       ["bootstrap.css"
        "material-design-iconic-font.min.css"
        "re-com.css"]))

(def ^{:private true} HEAD-FONTS
  ["http://fonts.googleapis.com/css?family=Roboto:300,400,500,700,400italic"
   "http://fonts.googleapis.com/css?family=Roboto+Condensed:400,300"])

(def ^{:private true} BODY-JS
  ["/js/main.js"])

;; =============================================================================
;; Components

(defn- head [title css]
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge"}]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
   (apply include-css (concat HEAD-CSS css HEAD-FONTS))
   [:title title]])

;; =============================================================================
;; API

;; TODO: http://getbootstrap.com/examples/cover/

(defn base [content & {:keys [css] :or {css []}}]
  (html5
   {:lang "en"}
   (head "Starcity" (map css-path css))
   [:body
    content
    ;; TODO: advanced cljs compilation
    ;; (apply include-js BODY-JS)
    ]))
