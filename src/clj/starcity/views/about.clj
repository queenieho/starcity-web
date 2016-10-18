(ns starcity.views.about
  (:require [starcity.views.page :as p]
            [starcity.views.components.layout :as l]
            [starcity.views.components.image :as i]))

(def ^:private content
  (l/section
   {:class "is-fullheight"}
   (l/container
    [:h1.title.is-1
     "We're a small team tackling a <b>big problem</b>."]
    [:p.subtitle.is-4
     {:style "margin-bottom: 60px;"}
     "Starcity is focused on solving the housing affordability crisis in cities,
     starting with <b>San Francisco</b>."]
    (l/columns
     (l/column
      {:class "is-half"}
      [:div.content.is-medium
       [:p
        "New supply of low income housing is made possible through government
        subsidies and public vouchers, and we all know that there's no shortage
        of luxury high-rises going up &mdash; but what about everyone in between?"]

       [:p
        "Building housing for those that make a normal income is <b>really,
        really difficult</b>; it involves navigating physical, capital,
        political and emotional pressures that are usually in conflict. As a
        result, not many people are working on this problem."]

       [:p
        "We at Starcity welcome this challenge because we don't want to lose the
        amazing character of the cities we love, like San Francisco."]

       [:p "Our mission is to <b>house the backbone of every city</b>: the
       teachers, police officers, firefighters, baristas, students, servers and
       entrepreneurs, making cities accessible to <em>everyone</em>."]])
     (l/column
      {:class "is-half"}
      (i/image "/assets/img/team.jpg")))

    [:div.content.is-medium
     [:p "Our solution will help address this generational problem by fitting
       more people into buildings without sacrificing quality, comfort or
       privacy. We do this by <b>optimizing the layout of buildings for modern
       communites</b>. We play by the rules, follow code, and are always sure to
       design with our residents (or <em>members</em> as we call them) in
       mind."]

     [:p "We promise to build what people want instead of what we <em>think</em>
       they want. Our members tell us that they value <b>experiences and
       relationships</b> over ownership and things."]

     [:p "We're Starcity: <strong>we build beautiful, comfortable communal
       housing</strong>...and we've only just begun. Join us to help build the future of
       housing!"]])))

(def about
  (p/page
   (p/title "About Us")
   p/navbar
   content))
