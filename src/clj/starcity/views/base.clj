(ns starcity.views.base
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-css include-js]]))

;;; =============================================================================
;; Constants

;; (def ^{:private true} HEAD-JS
;;   [])

(def BASE-CSS-DIR "assets/css")

(def BASE-JS-DIR "js")

(defn- css-path [filename]
  (format "/%s/%s" BASE-CSS-DIR filename))

(defn- js-path [filename]
  (format "/%s/%s" BASE-JS-DIR filename))

(def ^{:private true} HEAD-CSS
  (map css-path ["bootstrap.css" "common.css"]))

(def ^{:private true} HEAD-FONTS
  ["https://fonts.googleapis.com/css?family=Roboto:300,400,500,700,400italic"
   "https://fonts.googleapis.com/css?family=Roboto+Condensed:400,300"
   "https://fonts.googleapis.com/css?family=Lato:300,400,700"
   "https://fonts.googleapis.com/css?family=Raleway"])

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
       [:link {:rel "icon" :type "image/png" :sizes "192x192" :href "/android-icon-192x192.png"}]
       [:link {:rel "icon" :type "image/png" :sizes "32x32" :href "/favicon-32x32.png"}]
       [:link {:rel "icon" :type "image/png" :sizes "96x96" :href "/favicon-96x96.png"}]
       [:link {:rel "icon" :type "image/png" :sizes "16x16" :href "/favicon-16x16.png"}]
       [:link {:rel "manifest" :href "/manifest.json"}]
       [:meta {:name "msapplication-TileColor" :content "#ffffff"}]
       [:meta {:name "msapplication-TileImage" :content "/ms-icon-144x144.png"}]
       [:meta {:name "theme-color" :content "#ffffff"}]
       (apply include-css (concat HEAD-CSS css HEAD-FONTS))
       [:title title]])))

(defn- navbar [nav-items nav-buttons]
  (letfn [(-nav-item [[text link attrs]]
            [:li [:a (merge {:href link} attrs) text]])
          (-nav-btn [[text link attrs]]
            [:a.btn.navbar-btn (merge {:href link} attrs) text])]
    [:nav.navbar.navbar-default
     [:div.container
      [:div.navbar-header
       [:button.navbar-toggle.collapsed
        {:type          "button"
         :data-toggle   "collapse"
         :data-target   "#navbar-collapse"
         :aria-expanded "false"}
        [:span.sr-only "Toggle navigation"]
        (for [_ (range 3)]
          [:span.icon-bar])]
       [:a.navbar-brand {:href "/"}
        [:img {:alt "Starcity Logo" :src "/assets/img/starcity-logo-wordmark.png"}]]]
      [:div#navbar-collapse.navbar-collapse.collapse
       (map -nav-btn nav-buttons)
       [:ul.nav.navbar-nav.navbar-right
        (map -nav-item nav-items)]]]]))

(defn- wrap-content [content nav-items nav-buttons]
  [:div.navbar-wrapper
   [:div.container-fluid
    (navbar nav-items nav-buttons)
    content]])

;; TODO: Consolidate these to one thing
(def ^:private default-nav-items
  [["Availability" "/availability"]
   ["FAQ" "/fack"]])

(def ^:private default-nav-buttons
  [["Apply Now" "/signup" {:class "btn-attention navbar-right"}]])

;; =============================================================================
;; API

(defn base [content & {:keys [body-class css js nav-items nav-buttons cljs-devtools? wrap? footer?]
                       :or   {body-class ""
                              css        []
                              js         []
                              nav-items  default-nav-items
                              nav-buttons default-nav-buttons
                              wrap?      true
                              footer?    true}}]
  (html5
   {:lang "en"}
   (head "Starcity" (map css-path css))
   [:body {:class body-class}
    (if wrap?
      (wrap-content content nav-items nav-buttons)
      content)
    (when footer?
      [:footer.footer
       [:div.container
        [:div.footer-content
         [:p.pull-right [:a {:href "#"} "Back to top"]]
         [:p "&copy; 2016 Starcity Properties, Inc."]]]])
    (apply include-js (->> (map js-path js) (concat BODY-JS)))
    (when cljs-devtools?
      [:script "goog.require('user.devtools')"])]))
