(ns starcity.views.apply
  (:require [starcity.views.page :as p]
            [starcity.views.components.loading :as l]
            [starcity.models.apply :refer [application-fee]]
            [starcity.config.stripe :refer [public-key]]
            [starcity.countries :refer [countries-json]]
            [clojure.java.io :as io]
            [cheshire.core :as json])
  (:refer-clojure :exclude [apply]))

(defn apply [email]
  (p/cljs-page "apply"
               (p/title "Apply")
               (p/navbar)
               [:section#apply.section l/hero-section]
               p/footer
               (p/json ["stripe" {:amount application-fee
                                  :key    public-key
                                  :email  email}]
                       ["countries" countries-json])
               (p/scripts "https://checkout.stripe.com/checkout.js")))
