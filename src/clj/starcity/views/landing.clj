(ns starcity.views.landing
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

(defn- subtitle [& content]
  [:p.subtitle.is-4 content])

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
    [:h2.title.is-1.has-text-left
     "<b>Home</b> is where <b>people</b> are"]
    (l/columns
     {:class "is-vcentered"}
     (l/column
      {:class "is-two-thirds"}
      (i/image "/assets/img/landing-2.jpg"))
     (l/column
      ;; [:p.subtitle.is-4
      ;;  "Our communities are <b>inclusive</b> and <b>uplifting</b>"]
      (subtitle "Starcity is about more than the space you live in &mdash; it's about the <b>people you share it with</b>.")
      (subtitle "Our members come from all walks of life and from all over the world to share their experiences and values."))))))

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
      (subtitle
       "We provide <b>beautiful communal spaces</b> for all of the activites best done
       in the company of others.")
      (subtitle
       "We also respect the need for a <b>private space</b> of one's own.")
      (subtitle
       "By distributing space more purposefully, we're able to provide a <b>modern
       experience</b> at an <b>attainable price</b>.")
      (subtitle
       "And no bunk-beds."))
     (l/column
      {:class "is-two-thirds"}
      [:h2.title.is-2.has-text-left
       "<b>Home</b> is <b>comfortable</b>"]
      (i/image "/assets/img/landing-3.jpg"))))))

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
        [:p.subtitle.is-5 "Available <b>November 15, 2016</b>"]]])
     (l/column
      {:class "is-half"}
      [:div.card.is-fullwidth
       [:div.card-image
        [:a {:href "/communities/mission"}
         (i/image {:class "is-4by3"} "/assets/img/mission/card-banner.jpg")]]
       [:div.card-content
        [:p.title.is-4 "The Mission"]
        [:p.subtitle.is-5 "Available <b>January 1, 2017</b>"]]]))

    [:a.button.is-primary.is-large {:href "/signup"} "Apply Now"])))

;; =============================================================================
;; API
;; =============================================================================

(def landing
  (p/page
   "Starcity"
   banner
   community
   [:hr.is-marginless]
   comfortable
   [:hr.is-marginless]
   communities
   newsletter))
