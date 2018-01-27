(ns starcity.controllers.brand
  (:require [net.cgrand.enlive-html :as html]
            [starcity.controllers.common :as common]
            [facade.core :as facade]))


(html/defsnippet header "templates/brand/header.html" [:header] [])
(html/defsnippet subheader "templates/brand/subheader.html" [:header]
  [& [active]]
  [:.nav-item] (common/maybe-activate active))


(html/defsnippet root "templates/brand.html" [:main] [])
(html/defsnippet root-svg "templates/brand/svg.html" [:svg] [])


(html/defsnippet guidelines-admin "templates/brand/guidelines.html" [:main] [])

(html/defsnippet guidelines "templates/brand/guidelines.html" [:main] []
  [:.admin-only] nil)


(html/defsnippet guidelines-svg "templates/brand/guidelines/svg.html" [:svg] [])


(html/defsnippet downloads "templates/brand/downloads.html" [:main] [])
(html/defsnippet downloads-svg "templates/brand/downloads/svg.html" [:svg] [])


(html/defsnippet press "templates/brand/press.html" [:main] [])
(html/defsnippet press-svg "templates/brand/press/svg.html" [:svg] [])


(defn show-root
  "Show the brand guidelines page."
  [req]
  (->> (common/page req {:main        (root)
                         :header      (header)
                         :svg         (root-svg)
                         :css-bundles ["public.css"]
                         :js-bundles  ["main.js"]})
       (common/render-ok)))


(defn show-guidelines
  [{identity :identity :as req}]
  (println "IDENTITY:" (:identity req)
           (= :account.role/admin (:account/role identity)))
  (->> (common/page req {:main        (if (= :account.role/admin (:account/role identity))
                                        (guidelines-admin)
                                        (guidelines))
                         :header      (subheader :guidelines)
                         :svg         (guidelines-svg)
                         :css-bundles ["public.css"]
                         :js-bundles  ["main.js"]})
       (common/render-ok)))

(defn show-downloads
  [req]
  (->> (common/page req {:main        (downloads)
                         :header      (subheader :downloads)
                         :svg         (downloads-svg)
                         :css-bundles ["public.css"]
                         :js-bundles  ["main.js"]})
       (common/render-ok)))


(defn show-press
  [req]
  (->> (common/page req {:main        (press)
                         :header      (subheader :press)
                         :svg         (press-svg)
                         :css-bundles ["public.css"]
                         :js-bundles  ["main.js"]})
       (common/render-ok)))
