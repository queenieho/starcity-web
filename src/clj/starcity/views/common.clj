(ns starcity.views.common
  (:require [optimus.link :as link]
            [clojure.string :as string]
            [cheshire.core :as json]))

(def font-awesome-css
  "https://maxcdn.bootstrapcdn.com/font-awesome/4.6.3/css/font-awesome.min.css")

;; TODO: Rename, perhaps context?
(defn public-defaults
  [req & {:keys [js-bundles]}]
  {:head    {:stylesheets (link/bundle-paths req ["public.css"])}
   :scripts (link/bundle-paths req (concat ["main.js"] js-bundles))})

;; TODO: Rename
(defn app-defaults
  [req app-name & {:keys [stylesheets scripts json]
                   :or   {stylesheets []
                          scripts     []
                          json        []}}]
  {:head    {:stylesheets (concat stylesheets
                                  (link/bundle-paths req ["styles.css"]))}
   :scripts (concat scripts (link/bundle-paths req [(format "%s.js" app-name)]))
   :json    json
   :app-id  app-name
   :title   (string/capitalize app-name)})
