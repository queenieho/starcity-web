(ns starcity.views.old.base
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-css include-js]]
            [cheshire.core :as json]))

;;; =============================================================================
;; Constants

;; (def ^{:private true} HEAD-JS
;;   [])

(def BASE-CSS-DIR "assets/css")

(def BASE-JS-DIR "js")

(defn- css-path [filename]
  (format "/%s/%s" BASE-CSS-DIR filename))

(defn- js-path [filename]
  (if (re-matches #"^https?.+" filename)
    filename
    (format "/%s/%s" BASE-JS-DIR filename)))

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

;; =====================================
;; Nav Items

(defmulti nav-item
  "Different types of nav items yield different HTML depending on their :type."
  :type)

(defmethod nav-item :button [{:keys [text uri attrs]}]
  [:a.btn.navbar-btn (merge {:href uri} attrs) text])

(defmethod nav-item :link [{:keys [text uri attrs]}]
  [:li [:a (merge {:href uri} attrs) text]])

;; default
(defmethod nav-item nil [{:keys [text uri attrs]}]
  [:li [:a (merge {:href uri} attrs) text]])

;; =====================================
;; Navbar

(defn- navbar [nav-items]
  (let [is-button? #(= (:type %) :button)
        buttons    (filter is-button? nav-items)
        links      (remove is-button? nav-items)]
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
       (map nav-item buttons)
       [:ul.nav.navbar-nav.navbar-right
        (map nav-item links)]]]]))

(defn- wrap-content [content nav-items]
  [:div.navbar-wrapper
   [:div.container-fluid
    (navbar nav-items)
    content]])

(def ^:private default-nav-items
  [{:text "Availability" :uri "/availability"}
   {:text "FAQ" :uri "/fack"}
   {:text "Apply Now" :uri "/signup" :type :button :attrs {:class "btn-attention navbar-right"}}])

;; =============================================================================
;; API

(defn base [content & {:keys [body-class css js json nav-items cljs-devtools? wrap? footer?]
                       :or   {body-class ""
                              css        []
                              js         []
                              json       []
                              nav-items  default-nav-items
                              wrap?      true
                              footer?    true}}]
  (html5
   {:lang "en"}
   (head "Starcity" (map css-path css))
   [:body {:class body-class}
    (if wrap?
      (wrap-content content nav-items)
      content)
    (when footer?
      [:footer.footer
       [:div.container
        [:div.footer-content
         [:div.row
          [:div.col-sm-4
           [:ul.list-unstyled
            [:li [:a {:href "/fack"} "FAQ"]]
            [:li [:a {:href "/terms"} "Terms"]]
            [:li [:a {:href "/privacy"} "Privacy"]]]]]
         [:div
          [:p.pull-left {:style "margin-top: 20px;"} "&copy; 2016 Starcity Properties, Inc."]
          [:p.pull-right {:style "margin-top: 20px;"} [:a {:href "#"} "Back to top"]]]]]])
    (for [[name obj] json]
      [:script
       (format "var %s = %s" name (json/encode obj))])
    (apply include-js (->> (map js-path js) (concat BODY-JS)))
    (when cljs-devtools?
      [:script "goog.require('user.devtools')"])]))
