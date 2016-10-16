(ns starcity.views.signup.complete
  (:require [starcity.views.page :as p]
            [starcity.views.components
             [hero :as h]
             [layout :as l]]))

(defn- content [req]
  (h/hero
   {:class "is-fullheight is-success is-bold"}
   (h/head (p/navbar-inverse req))
   (h/body
    (l/container
     [:p.title.is-2
      "You're one step closer to a "
      [:strong "beautiful home"]
      " in the city."]
     [:p.subtitle.is-4 "You should receive an email from us shortly with an activation link &mdash; click that link to begin your " [:strong "member application."]]))))

(def complete
  (p/page
   (p/title "Signup Complete")
   (p/content
    content)))
