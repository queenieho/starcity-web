(ns starcity.controllers.terms
  (:require [selmer.parser :as selmer]
            [starcity.views.common :refer [public-defaults]]))

(defn show
  "Show the Terms of Service page."
  [req]
  (selmer/render-file "terms.html" (public-defaults req)))
