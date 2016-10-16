(ns starcity.views.bulma.landing
  (:require [starcity.views.page :as p]
            [starcity.views.components
             [navbar :refer [navbar nav-item]]
             [hero :as hero]]
            [hiccup.core :refer [html]]
            [hiccup.element :refer [link-to mail-to unordered-list]]
            [starcity.views.components.layout :as l]))

;; =============================================================================
;; Internal
;; =============================================================================

(defn- gradient-background-image
  [url]
  {:style (format "background-image: linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url('%s');" url) :class "has-background-image"})

;; =============================================================================
;; Banner

(def ^:private hero-head
  (comp hero/head p/navbar-inverse))

(def ^:private hero-body
  (html
   (hero/body
    [:div.container
     [:h1.title.is-1 "Get <strong>more</strong> from your home"]
     [:h2.subtitle.is-4
      "Comfortable, communal housing in " [:strong "San Francisco"]]
     [:a.button.is-primary.is-large {:href "/signup"}
      "Apply Now"]])))

(defn- banner [req]
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
;; Community

(def ^:private community
  (l/section
   {:class "is-fullheight"}
   (l/container
    [:h2.title.is-2.has-text-left
     "<b>Home</b> is where <b>people</b> are"]
    (l/columns
     {:class "is-vcentered"}
     (l/column
      {:class "is-two-thirds"}
      [:figure.image
       [:img {:src "/assets/img/landing-2.jpg"}]])
     (l/column
      ;; [:p.subtitle.is-5
      ;;  ]
      [:p.subtitle.is-5
       "Starcity is about more than the space you live in. It's about the people you share it with."]
      )))))

;; =============================================================================
;; Comfortable

(def ^:private comfortable
  (l/section
   {:class "is-fullheight"}
   (l/container
    (l/columns
     {:class "is-vcentered"}
     (l/column
      {:class "is-one-third has-text-left"}
      [:p.subtitle.is-5
       "While we provide beautiful communal spaces for all of the activites
         best done in the company of others, we also recognize the need for a
         space of one's own."]
      [:p.subtitle.is-5
       "Spacious <strong>private rooms</strong> come <strong>fully
         furnished</strong> with brand-new mattresses, high-quality textiles and
         modern furniture."]

      ;; [:div.tile.is-parent.is-vertical
      ;;  [:article.tile.is-child
      ;;   [:p.subtitle.is-5
      ;;    "While we provide beautiful communal spaces for all of the activites
      ;;    best done in the company of others, we also recognize the need for a
      ;;    space of one's own."]]
      ;;  [:article.tile.is-child
      ;;   [:p.subtitle.is-5
      ;;    "Spacious <strong>private rooms</strong> come <strong>fully
      ;;    furnished</strong> with brand-new mattresses, high-quality textiles and
      ;;    modern furniture."]]
      ;;  [:article.tile.is-child.has-text-centered
      ;;   [:p.subtitle.is-4
      ;;    "All memberships include:"]]
      ;;  [:article.tile.is-child.has-text-centered
      ;;   [:p.heading "Weekly Cleaning"]
      ;;   [:figure.image.is-64x64
      ;;    {:style "margin-right: auto; margin-left: auto;"}
      ;;    [:img {:src "/assets/img/clean-icon-192x192.png"}]]]
      ;;  [:article.tile.is-child.has-text-centered
      ;;   [:p.heading "On-site Laundry"]
      ;;   [:figure.image.is-64x64
      ;;    {:style "margin-right: auto; margin-left: auto;"}
      ;;    [:img {:src "/assets/img/washing-machine-icon-192x192.png"}]]]]
      )
     (l/column
      {:class "is-two-thirds"}
      [:h2.title.is-2.has-text-left
       "<b>Home</b> is <b>comfortable</b>"]
      [:figure.image
       [:img {:src "/assets/img/mission/rooms/large/room-1.jpg"}]]
      )))))

;; =============================================================================
;; API
;; =============================================================================

(def landing
  (p/page
   "Starcity"
   (p/content
    banner
    community
    [:hr.is-marginless]
    comfortable
    newsletter)))
