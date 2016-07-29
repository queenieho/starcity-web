(ns starcity.views.team
  (:require [starcity.views.base :refer [base]]))

(defn- founder
  [{:keys [name title description image]}]
  [:div.row
   [:div.col.l3.m4.s12
    [:img.circle.responsive-img {:src image}]]
   [:div.col.l9.m8.s12
    [:h4 name]
    [:h5.light.grey-text.lighten-4 title]
    [:p.flow-text-small description]]])

(def ^:private founders
  [{:name        "Jon Dishotsky"
    :title       "Founder &amp; CEO"
    :image       "/assets/img/jon.jpg"
    :description "Donec posuere augue in quam.  Aliquam erat volutpat.  Pellentesque condimentum, magna ut suscipit hendrerit, ipsum augue ornare nulla, non luctus diam neque sit amet urna.  Nam a sapien.  Nam a sapien.  Nam a sapien.  Sed diam.  Cras placerat accumsan nulla.  Pellentesque condimentum, magna ut suscipit hendrerit, ipsum augue ornare nulla, non luctus diam neque sit amet urna.  Nulla posuere."}
   {:name        "Mo Sakrani"
    :title       "Cofounder &amp; CPO"
    :image       "/assets/img/mo.jpg"
    :description "Donec posuere augue in quam.  Aliquam erat volutpat.  Pellentesque condimentum, magna ut suscipit hendrerit, ipsum augue ornare nulla, non luctus diam neque sit amet urna.  Nam a sapien.  Nam a sapien.  Nam a sapien.  Sed diam.  Cras placerat accumsan nulla.  Pellentesque condimentum, magna ut suscipit hendrerit, ipsum augue ornare nulla, non luctus diam neque sit amet urna.  Nulla posuere."}
   {:name        "Jesse Suarez"
    :title       "Cofounder &amp; COO"
    :image       "/assets/img/jesse.jpg"
    :description "Donec posuere augue in quam.  Aliquam erat volutpat.  Pellentesque condimentum, magna ut suscipit hendrerit, ipsum augue ornare nulla, non luctus diam neque sit amet urna.  Nam a sapien.  Nam a sapien.  Nam a sapien.  Sed diam.  Cras placerat accumsan nulla.  Pellentesque condimentum, magna ut suscipit hendrerit, ipsum augue ornare nulla, non luctus diam neque sit amet urna.  Nulla posuere."}
   {:name        "Josh Lehman"
    :title       "Cofounder &amp; CTO"
    :image       "/assets/img/josh.jpg"
    :description "Donec posuere augue in quam.  Aliquam erat volutpat.  Pellentesque condimentum, magna ut suscipit hendrerit, ipsum augue ornare nulla, non luctus diam neque sit amet urna.  Nam a sapien.  Nam a sapien.  Nam a sapien.  Sed diam.  Cras placerat accumsan nulla.  Pellentesque condimentum, magna ut suscipit hendrerit, ipsum augue ornare nulla, non luctus diam neque sit amet urna.  Nulla posuere."}])

(def ^:private content
  [:main
   [:div.container
    [:h2 "The Team"]
    [:div.card-panel
     (map founder founders)]]])

(defn team
  []
  (base :content content :title "Team"))
