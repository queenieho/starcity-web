(ns starcity.controllers.common
  (:require [clojure.string :as string]
            [facade.core :as facade :refer [maybe-prepend maybe-substitute]]
            [facade.optimus :as foptimus]
            [facade.html :as fhtml]
            [net.cgrand.enlive-html :as html :refer [deftemplate]]
            [ring.util.response :as response]))

;; ==============================================================================
;; utility ======================================================================
;; ==============================================================================


(defn ok
  [body]
  (-> (response/response body)
      (response/content-type "text/html")))


(defn render [t]
  (apply str t))


(def render-ok
  (comp ok render))


(defn malformed
  [body]
  (-> (response/response body)
      (response/status 400)
      (response/content-type "text/html")))


(def render-malformed
  (comp malformed render))


(defn- href->k [href]
  (when href (keyword (string/replace href "/" ""))))


(defn- maybe-activate
  [active]
  (letfn [(activate [node]
            (update-in node [:attrs :class] str " active"))]
    (if-let [k active]
      #(cond
         (= k (href->k (get-in % [:attrs :href])))       (activate %)
         (= (name k) (get-in % [:attrs :data-nav-item])) (activate %)
         :otherwise                                      %)
      identity)))


;; ==============================================================================
;; page =========================================================================
;; ==============================================================================


(html/defsnippet header "templates/partials/header.html" [:header]
  [& [active]]
  [:.nav-item] (maybe-activate active))


(deftemplate page "templates/base.html"
  [req {:keys [header svg main scripts fonts js-bundles css-bundles asset-path]
        :or   {fonts      [facade/default-fonts]
               asset-path "/assets/img/"}}]
  [:head] (html/do->
           (html/append (apply foptimus/css-bundles req css-bundles))
           (html/append (fhtml/font-links fonts)))
  [:body] (html/do->
           (maybe-prepend svg)
           (html/append
            (html/html
             (concat
              (fhtml/js-links scripts)
              (apply foptimus/js-bundles req js-bundles)))))
  [:header] (html/substitute (or header (starcity.controllers.common/header)))
  [:main] (maybe-substitute main)
  [:img] #(update-in % [:attrs :src] (foptimus/cache-bust-images req asset-path)))
