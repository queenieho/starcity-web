(ns starcity.views.communities.soma
  (:require [starcity.views.page :as p]
            [starcity.views.components
             [hero :as h]
             [layout :as l]
             [image :as i]]
            [hiccup.core :refer [html]]
            [hiccup.def :refer [defelem]]
            [starcity.views.communities.common :refer :all]))


;; =============================================================================
;; Internal
;; =============================================================================

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
       [:h1.title.is-1
        "Learn about how we built our <b>first communal house</b> in <b>SoMa</b>"]
       [:p.subtitle.is-4 [:em "Available November 15th, 2016"]]))))) )

(def ^:private june
  (l/section
   {:class "is-fullheight" :id "june"}
   (l/container
    {:class "has-text-center"}
    (title "In <b>June</b> of 2016, we struck a deal")
    (l/columns
     {:class "is-vcentered"}
     (l/column
      (subtitle "We discovered a two-bedroom home with an <b>under-utilized floorplan</b>.")
      (subtitle "We convinced the homeowner (the bald one) that a six-bedroom <b>communal home</b>
      was the <b>highest and best use</b> for this building.")
      (subtitle "<em>He agreed</em>."))
     (l/column
      {:class "is-half"}
      (i/image "/assets/img/soma/deal.jpg"))))))

(def ^:private july
  (l/section
   {:class "is-fullheight" :id "july"}
   (l/container
    (l/columns
     {:class "is-vcentered"}
     (l/column
      {:class "has-text-centered"}
      (title "<b>July</b> is the month that <b>demolition began</b> (and ended)"))
     (l/column
      {:class "is-one-third"}
      (i/image "/assets/img/soma/demo-2.jpg"))
     (l/column
      {:class "is-one-third"}
      (l/columns
       (l/column
        (i/image "/assets/img/soma/demo-1.jpg")))
      (l/column
       (i/image "/assets/img/soma/demo-3.jpg")))))))

(def ^:private august
  (l/section
   {:class "is-fullheight" :id "august"}
   (l/container
    (title
     "In <b>August</b> the building began its <b>new life</b>")
    (subtitle "Our challenge was to think of a layout that is <b>open</b> and
      <b>inviting</b>, yet <b>space-efficient</b>.")
    (subtitle "We started to see our designs take shape &mdash; framing new,
      <b>comfortably-sized</b> private rooms and a communal area <b>ideal for gatherings</b>.")
    (l/columns
     {:class "is-vcentered"}
     (l/column
      (i/image "/assets/img/soma/communal-demo.jpg"))
     (l/column
      (i/image "/assets/img/soma/framing.jpg"))
     (l/column
      (i/image "/assets/img/soma/stairwell.jpg"))))))

(def ^:private september
  (l/section
   {:class "is-fullheight" :id "september"}
   (l/container
    (title
     "In <b>September</b> we put <b>flesh on the bones</b>")
    (l/columns
     (l/column
      (i/image "/assets/img/soma/september-1.jpg"))
     (l/column
      (i/image "/assets/img/soma/september-2.jpg"))
     (l/column
      (i/image "/assets/img/soma/september-3.jpg"))))))

(def ^:private october
  (l/section
   {:class "is-fullheight" :id "october"}
   (l/container
    (title "<b>October</b> is a sprint towards the <b>finish line</b>")
    (l/columns
     (l/column
      (i/image "/assets/img/soma/october-1.jpg"))
     (l/column
      (i/image "/assets/img/soma/october-2.jpg"))
     (l/column
      (i/image "/assets/img/soma/october-3.jpg")))
    (l/columns
     (l/column
      (i/image "/assets/img/soma/october-4.jpg"))
     (l/column
      (i/image "/assets/img/soma/october-5.jpg"))
     (l/column
      (i/image "/assets/img/soma/october-6.jpg"))))))

(def ^:private november
  (let [fa-attrs {:style "color: black; font-size: 60px; height: 64px; width: 64px; line-height: 60px;"}]
    (l/section
    {:class "is-fullheight" :id "november"}
    (l/container
     {:class "has-text-centered is-vcentered"}
     (title "<b>November</b> is set for <b>move-in</b>, starting at <b>$2100/month</b>")
     (subtitle "We'd love to show you more &mdash; "
               [:a {:href "mailto:team@joinstarcity.com"} "contact us"]
               " now to schedule a tour!")
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

(def soma
  (p/page
   (p/title "West SoMa")
   banner
   june
   [:hr.is-marginless]
   july
   [:hr.is-marginless]
   august
   [:hr.is-marginless]
   september
   [:hr.is-marginless]
   october
   [:hr.is-marginless]
   november
   ))
