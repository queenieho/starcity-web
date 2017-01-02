(ns starcity.views.communities.soma
  (:require [starcity.views.page :as p]
            [starcity.views.components
             [hero :as h]
             [layout :as l]
             [image :as i]]
            [hiccup.core :refer [html]]
            [hiccup.def :refer [defelem]]
            [starcity.views.communities.common :as c]))


;; =============================================================================
;; Internal
;; =============================================================================

;; =============================================================================
;; Banner

(def ^:private title
  [:h1.title.is-1
   "Live in our <b>first community</b> in the <b>SoMa</b> neighborhood"])

(defn- banner [req]
  (h/background-image
   {:class "is-large is-primary is-fullheight"}
   "/assets/img/soma/banner.jpg"
   (h/head (p/navbar-inverse req))
   (h/body
    {:style "align-items: flex-end;"}
    (l/container
     (l/columns
      (l/column
       {:class "is-7 is-offset-5 has-text-right"}
       title
       [:p.subtitle.is-3
        "Join a <b>safe, quiet</b> and <b>diverse</b> home equipped with the <br>latest
        <b>smart-home</b> technology."]
       [:p.subtitle.is-4 [:em "Available Now"]]))))) )

;; =============================================================================
;; Rooms

(defn- room-image [num]
  (i/image
   {:class "opens-gallery" :data-room-num num}
   (format "/assets/img/soma/rooms/medium/room-%s.jpg" num)))

(def ^:private rooms
  (l/section
   {:class "is-fullheight" :id "rooms"}
   (l/container
    {:class "has-text-center"}
    (c/title "Brand new, <strong>fully-furnished</strong> rooms with <b>warmth &amp; personality</b>")
    (l/columns
     {:class "is-vcentered"}
     (l/column
      (room-image 1))
     (l/column
      (c/subtitle "Every private room comes with <b>all of the essentials</b>
     &mdash; queen bed, wardrobe, sink and high ceilings with plenty of natural
     light."))
     (l/column
      (room-image 3)))
    (l/columns
     {:class "is-vcentered"}
     (l/column
      (c/subtitle
       "Each room is designed with care to have its own <b>unique look and feel</b>
       that fits the character of the home and the eclectic SoMa neighborhood."))
     (l/column
      (room-image 6))
     (l/column
      (c/subtitle
       "Open your door <b>with your phone</b> &mdash; each room is equipped with an "
       [:strong [:a {:target "_blank" :href "http://august.com"} "August Lock"]]
       ", giving you full control and security."))))))

;; =============================================================================
;; Kitchen

(def ^:private kitchen
  (l/section
   {:id "kitchen" :class "is-fullheight"}
   (l/container
    (l/columns
     (l/column
      {:style "display: flex; flex-direction: column; justify-content: space-between;"}
      (c/title "A <b>spacious, open, state-of-the-art kitchen</b> is the focal point of the home")
      (c/subtitle "With <b>top-of-the-line appliances, polished concrete floors, and copious amounts of storage,</b> the kitchen is accessible to both the amateur and professional foodie.")
      (i/image "/assets/img/soma/communal/kitchen.jpg"))
     (l/column
      {:class "is-half"}
      (i/image
       {:style "width: 95%; margin-left: auto; margin-right: auto;"}
       "/assets/img/soma/communal/dining.jpg"))))))

;; =============================================================================
;; Neighborhood

(def ^:private neighborhood
  (l/section
   {:id "neighborhood" :class "is-fullheight"}
   (l/container
    (c/title "Live in a neighborhood that is <b>constantly evolving</b> &amp; <b>growing</b>")
    (l/columns
     {:class "is-vcentered"}
     (l/column
      (c/subtitle "<b>SoMa</b> has grown from an industrial manufacturing hub to the headquarters of some of the most <b>innovative companies in the world</b>. "))
     (l/column
      (i/image "/assets/img/soma/neighborhood/historic-1.jpg"))
     (l/column
      (i/image "/assets/img/soma/neighborhood/historic-2.jpg")))
    (l/columns
     {:class "is-vcentered"}
     (l/column
      (i/image "/assets/img/soma/neighborhood/modern-3.jpg"))
     (l/column
      (i/image "/assets/img/soma/neighborhood/modern-4.jpg"))
     (l/column
      (c/subtitle "The character of the factories from the past has created a lasting aesthetic in the neighborhood lending to its <b>raw and earthy streetscapes</b>. Many of the production facilities still remain intact as the <b>&quot;maker culture&quot;</b> has made its resurgence."))))))

;; =============================================================================
;; Amenities

(def ^:private amenities
  (let [fa-attrs {:style "color: black; font-size: 60px; height: 64px; width: 64px; line-height: 60px;"}]
    (l/section
     {:class "is-fullheight"}
     (l/container
      {:class "has-text-centered"}
      (c/title "Starting at <b>$2000/month</b>, which includes:")
      [:nav.level
       {:style "margin-top: 60px; margin-bottom: 60px;"}
       [:div.level-item.has-text-centered
        [:p.heading "Smart-home Equipped"]
        (i/image
         {:class "is-64x64" :style "margin-right: auto; margin-left: auto;"}
         "/assets/img/smart-home-icon-192x192.png")]
       [:div.level-item.has-text-centered
        [:p.heading "All Utilities Included"]
        [:i.icon.fa.fa-lightbulb-o.is-large fa-attrs]]
       [:div.level-item.has-text-centered
        [:p.heading "On-site Laundry"]
        (i/image
         {:class "is-64x64" :style "margin-right: auto; margin-left: auto;"}
         "/assets/img/washing-machine-icon-192x192.png")]
       [:div.level-item.has-text-centered
        [:p.heading "Weekly Cleaning"]
        (i/image
         {:class "is-64x64" :style "margin-right: auto; margin-left: auto;"}
         "/assets/img/clean-icon-192x192.png")]]
      [:a.button.is-large.is-primary {:href "/signup"} "Apply Now"]))))

;; =============================================================================
;; API
;; =============================================================================

(def soma
  (p/page
   (p/title "West SoMa")
   banner
   rooms
   [:hr.is-marginless]
   kitchen
   [:hr.is-marginless]
   neighborhood
   [:hr.is-marginless]
   amenities
   [:hr.is-marginless]))
