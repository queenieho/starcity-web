(ns starcity.views.components.form
  (:require [hiccup.def :refer [defelem]]
            [hiccup.form :as f]))

(defelem control
  "A form control. Serves to preserve spacing of form elements."
  [& content]
  [:p.control content])

(defelem label
  [name text]
  (f/label {:class "label"} name text))
