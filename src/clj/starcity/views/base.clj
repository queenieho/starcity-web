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

(defn head []
  [:head
   ;; Icon Font
   [:link {:href "https://fonts.googleapis.com/icon?family=Material+Icons" :rel "stylesheet"}]
   [:link {:type  "text/css"
           :rel   "stylesheet"
           :href  main-css
           :media "screen,projection"}]

   ;; Let browser know whebsite is optimized for mobile
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]])

(defn footer []
  (let [company-links [["About" "/about"]
                       ["Team" "/team"]
                       ["Blog" "https://blog.starcityproperties.com"]]]
    [:footer.page-footer
     [:div.container
      [:div.col.l4.offset-l2.s12
       [:h5.white-text "Company"]
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

(def default-nav-links [nav/communities nav/faq nav/about nav/apply])

(defn hero
  "Page template with a `hero`."
  [& {:keys [nav-links content title description action background-image]
      :or   {nav-links default-nav-links
             content   [:div]}}]
  (html5
   {:lang "en"}
   (head)
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
  [& {:keys [nav-links content js json]
      :or   {nav-links default-nav-links
             js        []
             json      []}}]
  (html5
   {:lang "en"}
   (head)
   [:body
    (navbar nav-links)
    content
    (footer)
    (apply include-js (concat base-js js))
    (for [[name obj] json]
      [:script
       (format "var %s = %s" name (json/encode obj))])
    (include-js
           "/assets/bower/jquery-validation/dist/jquery.validate.js"
           "/js/validation-defaults.js" ; TODO: Bundle with /js/main.js
           "/js/main.js")
    ]))
