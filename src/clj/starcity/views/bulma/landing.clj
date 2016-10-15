(ns starcity.views.bulma.landing
  (:require [starcity.views.page :as p]
            [starcity.views.components
             [navbar :refer [navbar nav-item]]
             [hero :as hero]]
            [hiccup.core :refer [html]]
            [hiccup.element :refer [link-to mail-to unordered-list]]))

;; =============================================================================
;; Internal
;; =============================================================================

(defn- gradient-background-image
  [url]
  {:style (format "background-image: linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url('%s');" url) :class "has-background-image"})

;; =============================================================================
;; Hero

(defn- auth-item [req]
  (let [role          (get-in req [:identity :account/role])
        [uri content] (case role
                        :account.role/applicant ["/apply" "Resume Application"]
                        :account.role/pending   ["/onboarding" "Security Deposit"]
                        :account.role/admin     ["/admin" "Admin"]
                        ["/login" "Log In"])]
    (nav-item uri content :button)))

(defn- hero-head [req]
  (hero/head (p/navbar-inverse (auth-item req))))

(def ^:private hero-body
  (html
   (hero/body
    [:div.container
     [:h1.title.is-1 "Get <strong>more</strong> from your home"]
     [:h2.subtitle.is-4
      "Comfortable, communal housing in " [:strong "San Francisco"]]])))

(defn- hero-section [req]
  (let [attrs (merge (gradient-background-image "/assets/img/landing-banner.jpg")
                     {:class "has-background-image is-fullheight is-primary"})]
    (hero/hero attrs (hero-head req) hero-body)))

;; =============================================================================
;; Newsletter

(def ^:private newsletter-form
  (html
   [:form {:action "/" :method "POST"}
    [:div.control.is-grouped
     [:div.control.has-icon.is-expanded
      [:input.input
       {:required true :placeholder "Email Address" :name "email" :type "email"}]
      [:i.fa.fa-envelope]]
     [:div.control
      [:button.button.is-white.is-outlined {:type "submit"}
       "Subscribe"]]]]))

(def ^:private subscribe-success)

(defn- newsletter [{:keys [params]}]
  (let [did-subscribe (= (:newsletter params) "subscribed")]
    (hero/hero
     {:class "is-primary"}
     (hero/body
      [:div#newsletter.container
       [:div.columns.is-vcentered
        [:div.column.is-one-third.is-left
         [:p.title "Subscribe to our " [:strong "Newsletter"]]
         [:p.subtitle "Stay up-to-date on our latest buildings and more."]]
        [:div.column
         newsletter-form
         (when did-subscribe
           [:div.notification.is-success {:style "margin-top: 10px;"}
            "All set! We'll send you the latest."])]]]))))

;; =============================================================================
;; API
;; =============================================================================

(def landing
  (p/page
   "Starcity"
   (p/content
    hero-section
    [:section.section]
    newsletter)))
