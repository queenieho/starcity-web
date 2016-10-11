(ns starcity.views.signup.invalid-activation
  (:require [starcity.views.page :as p]
            [starcity.views.components
             [hero :as h]
             [layout :as l]]))

(def ^:private content
  (h/hero
   {:class "is-fullheight is-danger is-bold"}
   (h/head (p/navbar-inverse))
   (h/body
    (l/container
     [:p.title.is-2 "Invalid activation link"]
     [:p.subtitle.is-4 "Well that's a bummer. Send us an email at "
      [:strong [:a {:href "mailto:team@joinstarcity.com"} "team@joinstarcity.com"]]
      " to get things sorted out."]
     ))))

(def invalid-activation
  (p/page
   (p/title "Whoops!")
   (p/content
    content)))
