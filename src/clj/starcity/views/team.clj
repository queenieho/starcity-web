(ns starcity.views.team
  (:require [starcity.views.page :as p]
            [starcity.views.components
             [layout :as l]
             [image :as i]]
            [hiccup.element :refer [link-to]]))

(defn- founder
  [{:keys [name title description image]}]
  (l/column
   {:class "is-half has-text-centered"}
   [:div.box
    {:style "min-height: 360px;"}
    (i/image {:class "is-128x128 has-content-centered"} image true)
    [:p.title.is-4 [:strong name]]
    [:p.subtitle.is-4 title]
    [:p.has-text-left description]]))

(def ^:private founders
  [{:name        "Jon Dishotsky"
    :title       "CEO & Co-Founder"
    :image       "/assets/img/jon.jpg"
    :description "Jon has lived in San Francisco since rent for an apartment was ~$1,000 (a long time ago). He grew up in Palo Alto, where his family fostered a communal lifestyle with people from around the world. They shacked up in the family’s cottage and developed a deep sense of (hippie) community spirit. At Starcity, Jon shapes company culture, finds and closes deals to grow Starcity's network of communal homes, identifies future growth opportunities, and handles public relations."}
   {:name        "Mo Sakrani"
    :title       "CPO & Co-Founder"
    :image       "/assets/img/mo.jpg"
    :description "Mo has lived in shared, communal housing his entire life, from growing up in a tiny apartment with a family of six in NYC to his current San Francisco apartment that he shares with a group of four. At Starcity, Mo shapes the design aesthetic, manages software and physical product development, and works closely with our communities to address their needs."}
   {:name        "Jesse Suarez"
    :title       "COO & Co-Founder"
    :image       "/assets/img/jesse.jpg"
    :description "Jesse lived in a communal house during his undergraduate days at UC Berkeley. There he learned the power of sharing resources and community interaction. Jesse was living in San Francisco until rent increases caused him to move east to Oakland. At Starcity, Jesse structures relationships with our many partners, handles financial operations, and spearheads recruitment."}
   {:name        "Josh Lehman"
    :title       "CTO & Co-Founder"
    :image       "/assets/img/josh.jpg"
    :description "Josh recently lived on a tiny sailboat for several weeks during a race from Port Townsend, Washington to Ketchikan, Alaska. He now lives in Oakland, though he's often found with his camera in Golden Gate Park, taking photos of everyday San Franciscans. At Starcity, Josh builds all of our software and handles smart-home tech integrations for our communal homes."}])

(def ^:private banner
  (l/section
   (l/container
    [:h1.title.is-1 "<b>Hello</b>, it's nice to meet you."]
    [:p.subtitle.is-4 "Feel free to reach out to get in touch with us via email at "
     (link-to "mailto:team@joinstarcity.com" "team@joinstarcity.com")
     ", or give us a call at <u>415.496.9706</u>."]
    (l/columns
     {:class "is-multiline"
      :style "margin-top: 30px;"}
     (map founder founders)))))

(def team
  (p/page
   (p/title "Team")
   (p/content
    p/navbar
    banner
    )))
