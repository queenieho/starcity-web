(ns starcity.controllers.apply
  (:require [selmer.parser :as selmer]
            [starcity.config.stripe :refer [public-key]]
            [starcity.controllers.common :as common]
            [starcity.models.apply :refer [application-fee]]
            [starcity.views.common :refer [app-defaults font-awesome-css]]))

(defn show
  "Show the Apply app."
  [req]
  (common/ok
   (selmer/render-file "apply.html" (app-defaults req "apply"
                                                  :scripts ["https://checkout.stripe.com/checkout.js"]
                                                  :json [["stripe" {:amount application-fee
                                                                    :key    public-key}]]
                                                  :stylesheets [font-awesome-css]))))
