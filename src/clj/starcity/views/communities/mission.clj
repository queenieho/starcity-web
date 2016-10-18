(ns starcity.views.communities.mission
  (:require [starcity.views.page :as p]
            [starcity.views.components
             [hero :as h]
             [layout :as l]
             [photoswipe :as photoswipe]
             [image :as i]]
            [hiccup.core :refer [html]]
            [hiccup.def :refer [defelem]]
            [starcity.views.communities.common :refer :all]))

;; =============================================================================
;; Components
;; =============================================================================

;; =============================================================================
;; Banner

(defn- banner [req]
  (h/background-image
   {:class "is-large is-primary is-fullheight"}
   "/assets/img/mission/banner.jpg"
   (h/head (p/navbar-inverse req))
   (h/body
    {:style "align-items: flex-end;"}
    (l/container
     (l/columns
      (l/column
       {:class "is-8 is-offset-4 has-text-right"}
       [:h1.title.is-1
        "Join a community in the heart of the " [:strong "Mission District"]]
       [:p.subtitle.is-3
        "Discover your "
        [:b [:a {:href "#rooms"} "new room"]]
        ", amazing "
        [:b [:a {:href "#communal"} "communal space"]]
        ", and the eclectic "
        [:b [:a {:href "#neighborhood"} "neighborhood"]]
        " that you'll be joining."
        ;; ", and the "
        ;; [:b [:a {:href "#people"} "people"]]
        ;; " that made it possible."
        ]
       [:p.subtitle.is-4 [:i "Coming January 1, 2017"]]))))))

;; =============================================================================
;; Rooms

(def ^:private rooms-title
  (html
   (title
    "Brand new, "
    [:strong "fully-furnished"]
    " rooms"
    "&mdash; just bring yourself.")))

(defn- room-image [num]
  (i/image
   {:class "opens-gallery" :data-room-num num}
   (format "/assets/img/mission/rooms/medium/room-%s.jpg" num)))

(def ^:private rooms
  (l/section
   {:class "is-fullheight" :id "rooms"}
   (l/container
    rooms-title
    (l/columns
     {:class "is-vcentered"}
     (l/column
      (room-image 8))
     (l/column
      (subtitle "Every private room comes with <b>all of the essentials</b> &mdash; queen or full-sized
     bed, closet, sink and high ceilngs with plenty of natural light."))
     (l/column
      (room-image 18)))
    (l/columns
     {:class "is-vcentered"}
     (l/column
      (subtitle
       "We have worked hard to create <b>unique designs</b> that fit the character of the Mission."))
     (l/column
      (room-image 13))
     (l/column
      (subtitle
       "Bathrooms are distributed <b>evenly throughout the building</b> so you're never
     more than a few steps away from a clean shower."))))))

;; =============================================================================
;; Communal Space

(def ^:private communal-space
  (l/section
   {:id "communal" :class "is-fullheight"}
   (l/container
    (l/columns
     (l/column
      {:class "is-half"}
      (title
       {:style "margin-bottom: 64px;"}
       "A place for gathering that <br>" [:strong "feels like home"])
      (subtitle
       "The communal space that we are designing has all the creature comforts of
     a cozy home: living rooms, dining areas, restaurant-grade kitchens and
     media rooms.")
      (subtitle
       "<b>However, it's still under construction!</b> We're expecting construction to be complete by <b>January 1st,
       2017</b>."))
     (l/column
      {:class "is-half"}
      (l/column
       (i/image "/assets/img/mission/communal/communal-2.jpg"))))
    ))
  )

;; =============================================================================
;; Neighborhood

(def ^:private neighborhood
  (letfn [(mural-image [filename]
            (i/image (format "/assets/img/mission/neighborhood/%s.jpg" filename)))]
    (l/section
     {:id "neighborhood" :class "is-fullheight"}

     (l/container
      ;; {:class "has-text-centered"}
      (title "Live in a neighborhood with a <strong>vibrant cultural history</strong>")
      ;;(title "Enjoy the <strong>sights, eats, and places</strong> that define the Mission")
      (l/columns
       (l/column
        ;; TOP ROW
        (l/columns
         {:class "is-vcentered"}
         (l/column
          (subtitle
           "Apart from great food, nightlife, weather and parks, the Mission is
            known for the <b>beautiful and often politically-charged murals</b>
            decorating its buildings and alleyways."))
         (l/column
          (mural-image "mural_5"))
         (l/column
          (mural-image "mural_3"))
         (l/column
          (mural-image "mural_6")))
        ;; BOTTOM ROW
        (l/columns
         (l/column
          (mural-image "mural_7"))
         (l/column
          (mural-image "mural_1")))))))))

(comment
  (subtitle
   "The Mission is a vibrant place defined and influenced by the many ethnic
       groups who have called the district home. From Spanish missionaries, Irish
       and German migrants to the Latino peoples and Central Americans, the
       Mission has long been a place where people have flocked to create better
       lives for themselves.")
  )

;; =============================================================================
;; The People

;; TODO: Revisit this closer to building completion

(comment
  (def ^:private people
    (l/section
     {:id "people" :class "is-fullheight"}
     (l/container
      (title "Learn about the <strong>people</strong> that made this building possible")
      ;; (subtitle )
      (l/columns
       (l/column
        {:class "is-4"}
        (l/box
         (i/image "/assets/img/mission/people/rex.png")))
       (l/column
        {:class "is-4"}
        (l/box
         (i/image {:class "is-square"} "/assets/img/mission/people/jacqueline.jpg")))
       (l/column
        {:class "is-4"}
        (l/box
         (i/image "/assets/img/mission/people/murilo.png"))))))))

;; =============================================================================
;; Features

(def ^:private features
  (let [fa-attrs {:style "color: black; font-size: 60px; height: 64px; width: 64px; line-height: 60px;"}]
    (l/section
     {:class "is-fullheight"}
     (l/container
      {:class "has-text-centered"}
      (title "Starting at <b>$2100/month</b>, which includes:")
      [:nav.level
       {:style "margin-top: 60px; margin-bottom: 60px;"}
       [:div.level-item.has-text-centered
        [:p.heading "Enterprise-grade Wifi"]
        [:i.icon.fa.fa-wifi.is-large fa-attrs]]
       [:div.level-item.has-text-centered
        [:p.heading "All Utilities"]
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

(def mission
  (p/page
   (p/title "The Mission")
   banner
   rooms
   [:hr.is-marginless]
   communal-space
   [:hr.is-marginless]
   neighborhood
   [:hr.is-marginless]
   features
   ;; people
   photoswipe/element
   (p/scripts "/assets/bower/photoswipe/dist/photoswipe.js"
              "/assets/bower/photoswipe/dist/photoswipe-ui-default.js")))
