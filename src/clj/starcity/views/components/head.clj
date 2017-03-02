(ns starcity.views.components.head
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [include-css]]))


;; =============================================================================
;; Constants
;; =============================================================================

(def font-awesome-url
  "https://maxcdn.bootstrapcdn.com/font-awesome/4.6.3/css/font-awesome.min.css")

(def josefin-sans-url
  "https://fonts.googleapis.com/css?family=Josefin+Sans")

(def lato-url
  "https://fonts.googleapis.com/css?family=Lato:300,400,700,900")

;; =============================================================================
;; Internal
;; =============================================================================

(defn- apple-touch-icon [size]
  [:link {:rel   "apple-touch-icon"
          :sizes size
          :href  (format "/apple-icon-%s.png" size)}])

;; =============================================================================
;; API
;; =============================================================================

(defn head [title & css-bundles]
  (let [sizes ["57x57" "60x60" "72x72" "76x76" "114x114" "120x120" "144x144"
               "152x152" "180x180"]]
    (html
     [:head
      [:meta {:charset "utf-8"}]
      [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge"}]
      ;; Let browser know whebsite is optimized for mobile
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
      (map apple-touch-icon sizes)
      [:link {:rel "icon" :type "image/png" :sizes "192x192" :href "/android-icon-192x192.png"}]
      [:link {:rel "icon" :type "image/png" :sizes "32x32" :href "/favicon-32x32.png"}]
      [:link {:rel "icon" :type "image/png" :sizes "96x96" :href "/favicon-96x96.png"}]
      [:link {:rel "icon" :type "image/png" :sizes "16x16" :href "/favicon-16x16.png"}]
      [:link {:rel "manifest" :href "/manifest.json"}]
      [:meta {:name "msapplication-TileColor" :content "#ffffff"}]
      [:meta {:name "msapplication-TileImage" :content "/ms-icon-144x144.png"}]
      [:meta {:name "theme-color" :content "#ffffff"}]
      (include-css josefin-sans-url lato-url font-awesome-url)
      css-bundles
      [:title title]])))
