(ns starcity.controllers.story
  (:require [selmer.parser :as selmer]
            [starcity.views.common :refer [public-defaults]]))

(defn show
  "Show the story page."
  [req]
  (selmer/render-file "story.html" (public-defaults req)))
