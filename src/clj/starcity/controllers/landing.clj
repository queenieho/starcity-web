(ns starcity.controllers.landing
  (:require [blueprints.models.property :as property]
            [clj-time.coerce :as c]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [datomic.api :as d]
            [facade.core :as facade]
            [net.cgrand.enlive-html :as html]
            [optimus.link :as link]
            [starcity.controllers.common :as common]
            [starcity.datomic :refer [conn]]))

;; =============================================================================
;; Helpers
;; =============================================================================


;; See https://github.com/cgrand/enlive/issues/110
(html/set-ns-parser! facade/hickory-parser)

(def ^:private hero-image-names
  (map
   (partial str "/assets/img/")
   ["hero-home2.jpg" "hero-home7.jpg" "hero-home10.jpg"]))

(defn- hero-images [req]
  (map (partial link/file-path req) hero-image-names))

(defn- bg-img [img-url]
  (format "background-image: linear-gradient(rgba(0, 0, 0, 0.25), rgba(0, 0, 0, 0.50)), url(%s)"
          img-url))

(def ^:private formatter (f/formatter "MMMM d, yyyy"))

(defn- availability
  [conn internal-name]
  (let [property     (property/by-internal-name (d/db conn) internal-name)
        available-on (c/to-date-time (property/available-on property))]
    (if (t/after? (t/now) available-on)
      "Now"
      (f/unparse formatter available-on))))


;; =============================================================================
;; Snippets
;; =============================================================================


(html/defsnippet svg "templates/landing/svg.html" [:svg] [])
(html/defsnippet header "templates/landing/header.html" [:header] [])
(html/defsnippet main "templates/landing.html" [:main] [conn hero-img]
  [:#hero] (html/set-attr :style (bg-img hero-img))
  [:#mission-card :.availability] (html/content (format "Available %s" (availability conn "2072mission"))))


;; =============================================================================
;; Handlers
;; =============================================================================


(defn show
  "Show the landing page."
  [req]
  (let [hero (rand-nth (hero-images req))]
    (common/render-ok
     (common/page req {:svg         (svg)
                       :css-bundles ["public.css"]
                       :js-bundles  ["main.js"]
                       :scripts     ["https://cdn.jsdelivr.net/gh/kenwheeler/slick@1.8.1/slick/slick.min.js"]
                       :header      (header)
                       :main        (main conn hero)}))))
