(ns starcity.views.base
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-css include-js]]
            [clojure.string :refer [lower-case]]
            [cheshire.core :as json]
            [starcity.views.base.nav :as nav]))

;; =============================================================================
;; Helpers
;; =============================================================================

(def ^:private materialize-js "/assets/bower/Materialize/dist/js/materialize.min.js")
(def ^:private main-css "/assets/css/main.css")

(def ^:private base-js
  ["https://code.jquery.com/jquery-2.1.1.min.js"
   materialize-js])

(defn- hero-background-style
  [url]
  {:style (format "background-image: linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url('%s');" url)})

;; =============================================================================
;; Components
;; =============================================================================

;; =============================================================================
;; Hero

(defn- hero-content
  [title description {:keys [uri text class] :as action}]
  [:div#hero
   [:div#hero-text-container.container

    [:div.row
     [:div.col.s12.center-align
      [:h1#hero-title {:itemprop "title"}
       title]]]
    [:div.row
     [:div.col.s12.center-align
      [:h2#hero-description {:itemprop "description"}
       description]]]
    [:div.row
     [:div.col.s12.center-align
      [:a.btn.btn-large.hero-btn {:href uri :class class}
       text]]]]])

;; =============================================================================
;; Generic

(defn- apple-touch-icon [size]
  [:link {:rel   "apple-touch-icon"
          :sizes size
          :href  (format "/apple-icon-%s.png" size)}])

(defn head [title]
  (let [sizes ["57x57" "60x60" "72x72" "76x76" "114x114" "120x120" "144x144"
               "152x152" "180x180"]]
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge"}]
     ;; google site verification
     [:meta {:name "google-site-verification" :content "efd7Gz_b7RGhSoL42WIElePfRXEZlKgguT-2ha5Zlqs"}]
     ;; Let browser know whebsite is optimized for mobile
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
     (map apple-touch-icon sizes)
     [:link {:rel "icon" :type "image/png" :sizes "192x192" :href "/android-icon-192x192.png"}]
     [:link {:rel "icon" :type "image/png" :sizes "32x32" :href "/favicon-32x32.png"}]
     [:link {:rel "icon" :type "image/png" :sizes "96x96" :href "/favicon-96x96.png"}]
     [:link {:rel "icon" :type "image/png" :sizes "16x16" :href "/favicon-16x16.png"}]
     [:link {:rel "manifest" :href "/manifest.json"}]
     [:meta {:name "msapplication-TileColor" :content "#ffffff"}]
     [:meta {:name "msapplication-TileImage" :content "/ms-icon-144x144.png"}]
     [:meta {:name "theme-color" :content "#ffffff"}]
     ;; Icon Font
     [:link {:href "https://fonts.googleapis.com/icon?family=Material+Icons" :rel "stylesheet"}]
     [:link {:type "text/css" :rel "stylesheet" :href main-css :media "screen,projection"}]
     [:title title]]))

(defn footer []
  (let [company-links [["About Us" "/about"]
                       ["Our Team" "/team"]
                       ["Blog" "https://blog.joinstarcity.com"]]]
    [:footer.page-footer
     [:div.container
      [:div.col.l4.offset-l2.s12
       [:h5.white-text "Starcity"]
       [:ul
        (for [[text uri] company-links]
          [:li [:a.grey-text.text-lighten-3 {:href uri} text]])]]]
     [:div.footer-copyright
      [:div.container
       "&copy; 2016 Starcity Properties, Inc."
       [:a.grey-text.text-lighten-4.right {:href "/privacy"} "Privacy"]
       [:a.grey-text.text-lighten-4.right {:href "/terms"} "Terms of Service"]]]]))

(def ^:private brand-logo
  [:a.brand-logo {:href "/"}
   [:img#header-logo {:alt "Starcity Logo" :src "/assets/img/starcity-brand-icon-white.png"}]
   [:span "Starcity"]])

(defn navbar
  ([links]
   (navbar links ""))
  ([links nav-class]
   [:header
    [:nav {:class nav-class}
     [:div.nav-wrapper.container
      brand-logo
      [:a.button-collapse
       {:href "#" :data-activates "mobile-menu"}
       [:i.material-icons "menu"]]
      [:ul.right.hide-on-small-and-down
       (for [link links] link)]
      [:ul#mobile-menu.side-nav
       (for [link links] link)]]]]))

;; =============================================================================
;; API
;; =============================================================================

(def default-nav-links [nav/communities nav/faq nav/apply])

(defn hero
  "Page template with a `hero`."
  [& {:keys [nav-links content title description action background-image head-title]
      :or   {nav-links  default-nav-links
             head-title "Starcity"
             content    [:div]}}]
  (html5
   {:lang "en"}
   (head head-title)
   [:body
    [:div#hero-wrapper (hero-background-style background-image)
     (navbar nav-links "transparent black-text")
     (hero-content title description action)]
    content
    (footer)
    (apply include-js base-js)
    (include-js "/js/hero.js")]))

(defn base
  "Page template with a solid navbar."
  [& {:keys [nav-links content js json title]
      :or   {nav-links default-nav-links
             js        []
             json      []}}]
  (html5
   {:lang "en"}
   (head (if title (str "Starcity &mdash; " title) "Starcity"))
   [:body
    (navbar nav-links)
    content
    (footer)
    (for [script (concat base-js js)]
      [:script {:src script :type "application/json"}])
    (for [[name obj] json]
      [:script {:type "application/json"}
       (format "var %s = %s" name (json/encode obj))])
    (for [script ["/assets/bower/jquery-validation/dist/jquery.validate.js"
                  "/js/validation-defaults.js" ; TODO: Bundle with /js/main.js
                  "/js/main.js"]]
      [:script {:src script :type "application/json"}])]))
