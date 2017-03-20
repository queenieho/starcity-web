(ns starcity.controllers.privacy
  (:require [selmer.parser :as selmer]
            [starcity.views.common :refer [public-defaults]]))

(defn show
  "Show the Privacy Policy page."
  [req]
  (selmer/render-file "privacy.html" (public-defaults req)))
