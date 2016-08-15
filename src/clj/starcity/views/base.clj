(ns starcity.views.base
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-css include-js]]
            [clojure.string :refer [lower-case]]
            [cheshire.core :as json]))

;; =============================================================================
;; Helpers
;; =============================================================================

(def ^:private materialize-js "/assets/bower/Materialize/dist/js/materialize.min.js")
(def ^:private main-css "/assets/css/main.css")

(def ^:private google-analytics
  [:script
   "(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','https://www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-81813253-1', 'auto');
  ga('send', 'pageview');"])

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
      [:div.row
       [:div.col.m3.s6
        [:h5.white-text "Company"]
        [:ul
         (for [[text uri] company-links]
           [:li [:a.grey-text.text-lighten-3 {:href uri} text]])]]
       [:div.col.m3.s6
        [:h5.white-text "Contact Us"]
        [:ul
         [:li [:a.grey-text.text-lighten-3 {:href "mailto:team@joinstarcity.com"}
               "team@joinstarcity.com"]]
         [:li.grey-text.text-lighten-3 "(415) 496-9706"]]]]]
     [:div.footer-copyright
      [:div.container
       "&copy; 2016 Starcity Properties, Inc."
       [:a.grey-text.text-lighten-4.right {:href "/privacy"} "Privacy"]
       [:a.hide-on-small-only.grey-text.text-lighten-4.right {:href "/terms"}
        "Terms of Service"]
       [:a.hide-on-med-and-up.grey-text.text-lighten-4.right {:href "/terms"}
        "Terms"]]]]))

(def ^:private brand-logo
  [:a.brand-logo {:href "/"}
   [:img#header-logo {:alt "Starcity Logo" :src "/assets/img/starcity-brand-icon-white.png"}]
   [:span "Starcity"]])

(defn- nav-link
  ([text]
   (nav-link text (format "/%s" (-> text lower-case))))
  ([text uri]
   [:li [:a {:href uri} text]]))

(defn- nav-button
  ([text]
   (nav-button text (format "/%s" (-> text lower-case))))
  ([text uri & classes]
   [:li
    [:a.waves-effect.waves-light.btn {:href uri :class (clojure.core/apply str classes)}
     text]]))

(def ^:private unauth-nav-items
  [(nav-link "Our Communities" "/communities")
   (nav-link "FAQ" "/faq")
   (nav-button "Apply Now" "/application" "star-orange")])

(def ^:private auth-nav-items
  [(nav-link "Our Communities" "/communities")
   (nav-link "FAQ" "/faq")
   (nav-link "Application" "/application")
   (nav-link "My Account" "/account")])

(def ^:private hero-navbar
  (let [links unauth-nav-items]
    [:header
     [:nav.transparent.black-text
      [:div.nav-wrapper.container
       brand-logo
       [:a.button-collapse
        {:href "#" :data-activates "mobile-menu"}
        [:i.material-icons "menu"]]
       [:ul.right.hide-on-small-and-down
        (for [link links] link)]
       [:ul#mobile-menu.side-nav
        (for [link links] link)]]]]))

(defn- navbar
  [{:keys [identity] :as req}]
  (let [links (if (nil? identity)
                unauth-nav-items
                auth-nav-items)]
    [:header
     (list
      [:nav
       [:div.nav-wrapper.container
        brand-logo
        [:a.button-collapse
         {:href "#" :data-activates "mobile-menu"}
         [:i.material-icons "menu"]]
        [:ul.right.hide-on-small-and-down
         (for [link links] link)]
        [:ul#mobile-menu.hide-on-med-and-up.side-nav
         (for [link links] link)]]])]))

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
;; API
;; =============================================================================

(defn hero
  "Page template with a `hero`."
  [& {:keys [content title description action background-image head-title]
      :or   {head-title "Starcity", content [:div]}}]
  (html5
   {:lang "en"}
   (head head-title)
   [:body
    [:div#hero-wrapper (hero-background-style background-image)
     hero-navbar
     (hero-content title description action)]
    content
    (footer)
    (apply include-js base-js)
    (include-js "/js/hero.js")
    google-analytics]))

(defn base
  "Page template with a solid navbar."
  [& {:keys [content js json title req]
      :or   {js [], json []}}]
  (html5
   {:lang "en"}
   (head (if title (str "Starcity &mdash; " title) "Starcity"))
   [:body
    (navbar req)
    content
    (footer)
    (apply include-js (concat base-js js))
    (for [[name obj] json]
      [:script
       (format "var %s = %s" name (json/encode obj))])
    (include-js
     "/assets/bower/jquery-validation/dist/jquery.validate.js"
     "/js/main.js")
    google-analytics]))
