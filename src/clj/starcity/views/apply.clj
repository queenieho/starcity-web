(ns starcity.views.apply
  (:require [starcity.views.page :as p]
            [starcity.views.components
             [loading :as l]
             [navbar :as n]]
            [starcity.models.apply :refer [application-fee]]
            [starcity.config.stripe :refer [public-key]]
            [starcity.countries :refer [countries-json]]
            [clojure.java.io :as io]
            [cheshire.core :as json])
  (:refer-clojure :exclude [apply]))

(def ^:private navbar
  (n/navbar
   false
   (n/nav-item "/communities" "Communities")
   (n/nav-item "/faq" "FAQ")
   (n/nav-item "/about" "About")
   (n/nav-button "/settings" "Account")))

(def apply
  (p/app
   "apply"
   (p/title "Apply")
   navbar
   [:section#apply.section l/hero-section]
   p/footer
   (p/json ["stripe" (fn []
                       {:amount application-fee
                        :key    public-key})]
           ["countries" (fn [] countries-json)])
   (p/scripts "https://checkout.stripe.com/checkout.js"
              "https://code.jquery.com/jquery-2.1.1.min.js"
              ;; "/js/main.js"
              )))
