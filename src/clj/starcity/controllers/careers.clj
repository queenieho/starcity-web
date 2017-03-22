(ns starcity.controllers.careers
  (:require [selmer.parser :as selmer]
            [starcity.controllers.common :as common]
            [starcity.views.common :refer [public-defaults]]))

(defn show
  "Show the Careers page."
  [req]
  (common/ok (selmer/render-file "careers.html" (public-defaults req))))
