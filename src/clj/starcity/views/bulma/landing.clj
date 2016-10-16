(ns starcity.views.bulma.landing
  (:require [starcity.views.page :as p]
            [starcity.views.components
             [navbar :refer [navbar nav-item]]
             [hero :as h]
             [image :as i]]
            [hiccup.core :refer [html]]
            [hiccup.element :refer [link-to mail-to unordered-list]]
            [starcity.views.components.layout :as l]
            [starcity.views.components.image :as i]))

;; =============================================================================
;; Internal
;; =============================================================================

;; =============================================================================
;; Banner

(def ^:private hero-head
  (comp h/head p/navbar-inverse))

(def ^:private hero-body
  (html
   (h/body
    (l/container
     [:h1.title.is-1 "Get <strong>more</strong> from your home"]
     [:h2.subtitle.is-4
      "Comfortable, communal housing in " [:strong "San Francisco"]]
     [:a.button.is-primary.is-large {:href "/signup"}
      "Apply Now"]))))

(defn- banner [req]
  (h/background-image
   {:class "is-fullheight is-primary"}
   "/assets/img/landing-banner.jpg"
   (hero-head req)
   hero-body))

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
    (h/hero
     {:class "is-primary"}
     (h/body
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
      (i/image "/assets/img/landing-2.jpg"))
     (l/column
      [:p.subtitle.is-5
       "Starcity is about more than the space you live in. It's about the people you share it with."])))))

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
       "While we provide beautiful communal spaces for all of the activites best
         done in the company of others, we also recognize the need for a space
         of one's own."]
      [:p.subtitle.is-5
       "Spacious <strong>private rooms</strong> come <strong>fully
         furnished</strong> with brand-new mattresses, high-quality textiles and
         modern furniture."])
     (l/column
      {:class "is-two-thirds"}
      [:h2.title.is-2.has-text-left
       "<b>Home</b> is <b>comfortable</b>"]
      (i/image "/assets/img/mission/rooms/large/room-1.jpg"))))))

;; =============================================================================
;; Communities

(def ^:private communities
  (l/section
   {:class "is-fullheight"}
   (l/container
    {:class "has-text-centered" :style "min-width: 70%;"}
    [:h2.title.is-2
     "Explore our <b>communities</b>"]
    (l/columns
     (l/column
      {:class "is-half"}
      [:div.card.is-fullwidth
       [:div.card-image
        [:div.card-image
         [:a {:href "/communities/soma"}
          (i/image {:class "is-4by3"} "/assets/img/soma/card-banner.jpg")]]]
       [:div.card-content
        [:p.title.is-4 "West SoMa"]
        [:p.subtitle.is-6 "Available <b>November 15, 2016</b>"]]
       [:div.card-footer
        [:a.card-footer-item
         {:href "/communities/soma"}
         "Learn More"]]])
     (l/column
      {:class "is-half"}
      [:div.card.is-fullwidth
       [:div.card-image
        [:a {:href "/communities/mission"}
         (i/image {:class "is-4by3"} "/assets/img/mission/card-banner.jpg")]]
       [:div.card-content
        [:p.title.is-4 "The Mission"]
        [:p.subtitle.is-6 "Available <b>January 1, 2017</b>"]]
       [:div.card-footer
        [:a.card-footer-item
         {:href "/communities/mission"}
         "Learn More"]]]))

    [:a.button.is-primary.is-large {:href "/signup"} "Apply Now"])))

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
    [:hr.is-marginless]
    communities
    newsletter)))
