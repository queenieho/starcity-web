(ns starcity.controllers.faq
  (:require [selmer.parser :as selmer]
            [starcity.views.common :refer [public-defaults]]))

(defn show
  "Show the FAQ page."
  [req]
  (selmer/render-file "faq.html" (public-defaults req)))
