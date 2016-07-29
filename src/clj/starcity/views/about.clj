(ns starcity.views.about
  (:require [starcity.views.base :refer [base]]))

(def ^:private content
  [:main
   [:div.container
    [:h2 "About Us"]
    [:p.flow-text "Based in San Francisco, California, Starcity is building beautifully-designed, comfortable homes for anyone interested in a community-focused lifestyle."]
    [:div.card-panel
     [:h4 "What We Believe"]
     [:p.flow-text-small "Home is welcoming, relaxing and safe. Home is a group of respectful, warm, and empathetic people. Home is an inclusive, uplifting community."]
    [:p.flow-text-small "Community-building in this city must reflect the eclectic nature of San Francisco itself. We are building communities that embrace individuals from all walks of life."]
    [:p.flow-text-small "Innovative space-design and smart home technology are important to building resource-efficient, aesthetically pleasing homes that foster community interaction and reduce our collective footprint."]
    [:p.flow-text-small "Balancing community space and resources with adequate private space is imperative to honor the needs of the workforce. We are designing private rooms in a way that will allow people to live sustainably in San Francisco for the long-term."]
    [:p.flow-text-small "Hard-working San Franciscans form the diverse fabric of our city, yet we are often left out of the housing conversation. We are engaging with the community at-large to facilitate a healthy dialogue about housing and to support initiatives that will bring more housing supply to the city."]
    [:p.flow-text-small "Living in San Francisco should be attainable to anyone open to a sustainable way of living with a focus on rewarding relationships, shared resources and a connection to the city."]]
    ]])

(defn about
  []
  (base :content content))
