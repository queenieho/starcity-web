(ns starcity.controllers.landing
  (:require [net.cgrand.enlive-html :as html]
            [starcity.controllers.common :as common]
            [starcity.views.base :as base]
            [optimus.link :as link]))

;; =============================================================================
;; Helpers
;; =============================================================================

;; See https://github.com/cgrand/enlive/issues/110
(html/set-ns-parser! base/hickory-parser)

;; GOOD: 1, 2, 6, 7
;; OK: 3 (!2), 8
;; BAD: 4, 5 (weird expr), 9 (weird expr)

(def ^:private hero-image-names
  (map
   (partial str "/assets/img/")
   ["hero-home3.jpg" "hero-home6.jpg" "hero-home7.jpg" "hero-home4.jpg"]))

(defn- hero-images [req]
  (map (partial link/file-path req) hero-image-names))

(defn- bg-img [img-url]
  (format "background-image: linear-gradient(rgba(0, 0, 0, 0.25), rgba(0, 0, 0, 0.50)), url(%s)"
          img-url))

;; =============================================================================
;; Snippets
;; =============================================================================

(html/defsnippet svg "templates/landing/svg.html" [:svg] [])
(html/defsnippet header "templates/landing/header.html" [:header] [])
(html/defsnippet main "templates/landing.html" [:main] [hero-img]
  [:#hero] (html/set-attr :style (bg-img hero-img)))

;; =============================================================================
;; Handlers
;; =============================================================================

(defn show
  "Show the landing page."
  [req]
  (let [hero (rand-nth (hero-images req))]
    (common/render-ok
     (base/public-base req
                       :svg (svg)
                       :header (header)
                       :main (main hero)))))
