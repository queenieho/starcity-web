(ns starcity.pages.base
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-css include-js]]))

;;; =============================================================================
;; Constants

;; (def ^{:private true} HEAD-JS
;;   [])

(def BASE-CSS-DIR "assets/css")

(def BASE-JS-DIR "js")

(defn- css-path [filename]
  (format "%s/%s" BASE-CSS-DIR filename))

(defn- js-path [filename]
  (format "%s/%s" BASE-JS-DIR filename))

(def ^{:private true} HEAD-CSS
  (map css-path
       ["bootstrap.css"
        "material-design-iconic-font.min.css"
        "re-com.css"]))

(def ^{:private true} HEAD-FONTS
  ["http://fonts.googleapis.com/css?family=Roboto:300,400,500,700,400italic"
   "http://fonts.googleapis.com/css?family=Roboto+Condensed:400,300"])

(def ^{:private true} BODY-JS
  (map js-path
       ["jquery.min.js"
        "bootstrap.min.js"]))

;; =============================================================================
;; Components

(defn- head [title css]
  (letfn [(apple-touch-icon [size]
            [:link {:rel   "apple-touch-icon"
                    :sizes size
                    :href  (format "/apple-icon-%s.png" size)}])]
    (let [sizes ["57x57" "60x60" "72x72" "76x76" "114x114" "120x120" "144x144"
                 "152x152" "180x180"]]
      [:head
       [:meta {:charset "utf-8"}]
       [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge"}]
       [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
       (map apple-touch-icon sizes)
       [:link {:rel "icon" :type "image/png" :sizes "192x192" :href "android-icon-192x192.png"}]
       [:link {:rel "icon" :type "image/png" :sizes "32x32" :href "favicon-32x32.png"}]
       [:link {:rel "icon" :type "image/png" :sizes "96x96" :href "favicon-96x96.png"}]
       [:link {:rel "icon" :type "image/png" :sizes "16x16" :href "favicon-16x16.png"}]
       [:link {:rel "manifest" :href "/manifest.json"}]
       [:meta {:name "msapplication-TileColor" :content "#ffffff"}]
       [:meta {:name "msapplication-TileImage" :content "/ms-icon-144x144.png"}]
       [:meta {:name "theme-color" :content "#ffffff"}]
       (apply include-css (concat HEAD-CSS css HEAD-FONTS))
       [:title title]])))

;; =============================================================================
;; API

(defn base [content & {:keys [body-class css js] :or {body-class "" css [] js []}}]
  (html5
   {:lang "en"}
   (head "Starcity" (map css-path css))
   [:body {:class body-class}
    content
    (apply include-js (->> (map js-path js) (concat BODY-JS)))]))
