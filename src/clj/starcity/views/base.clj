(ns starcity.views.base
  (:require [cheshire.core :as json]
            [clojure.string :as string]
            [hickory.core :as h]
            [net.cgrand.enlive-html :as html :refer [deftemplate]]
            [optimus.link :as link]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- optify
  "Helper that examines paths with the supplied prefix and either subs
  in their cache-busting URLs or returns them unchanged."
  [req prefix]
  (fn [^String src]
    (or (and (.startsWith src prefix)
             (not-empty (link/file-path req src)))
        src)))

(defn- css [& stylesheets]
  (map
   (fn [href]
     [:link {:href  href
             :rel   "stylesheet"
             :type  "text/css"
             :media "screen, projection"}])
   stylesheets))

(defn- css-bundles* [req & bundle-names]
  (map
   (fn [href]
     [:link {:href  href
             :rel   "stylesheet"
             :type  "text/css"
             :media "screen, projection"}])
   (link/bundle-paths req bundle-names)))

(defn- js* [scripts]
  (map
   (fn [src] [:script {:src src :type "text/javascript"}])
   scripts))

(defn- js-bundles* [req bundle-names]
  (js* (link/bundle-paths req bundle-names)))

(defn- fonts* [fonts]
  (map (fn [href] [:link {:href href :rel "stylesheet"}]) fonts))

(defn- json* [json]
  (map
   (fn [[name obj]]
     [:script (format "var %s=%s;" name (json/encode obj))])
   json))

(defmacro maybe-substitute
  ([expr] `(if-let [x# ~expr] (html/substitute x#) identity))
  ([expr & exprs] `(maybe-substitute (or ~expr ~@exprs))))

(defmacro maybe-prepend
  ([expr] `(if-let [x# ~expr] (html/prepend x#) identity))
  ([expr & exprs] `(maybe-prepend (or ~expr ~@exprs))))

(defmacro maybe-append
  ([expr] `(if-let [x# ~expr] (html/append x#) identity))
  ([expr & exprs] `(maybe-append (or ~expr ~@exprs))))

(defmacro maybe-content
  ([expr] `(if-let [x# ~expr] (html/content x#) identity))
  ([expr & exprs] `(maybe-content (or ~expr ~@exprs))))

(defn maybe-errors
  "If `errors` are non-nil, append them to the selected container; otherwise,
  hide the selected container."
  [errors]
  (if errors
    (->> errors
         (map (fn [e] [:div.alert.alert-error.mb2 e]))
         html/html
         html/append)
    (html/add-class "dn")))

(defn maybe-messages
  "If `errors` are non-nil, append them to the selected container; otherwise,
  hide the selected container."
  [msgs]
  (if msgs
    (->> msgs
         (map (fn [m] [:div.alert.alert-success.mb2 m]))
         html/html
         html/append)
    (html/add-class "dn")))

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

;; =============================================================================
;; Components
;; =============================================================================

;; See https://github.com/cgrand/enlive/issues/110
(defn hickory-parser
  "Loads and parse an HTML resource and closes the stream."
  [stream]
  (filter map? (map h/as-hickory (h/parse-fragment (slurp stream)))))

(def default-fonts
  "https://fonts.googleapis.com/css?family=Caveat|Eczar:700|Work+Sans:400,600")

(def lato-fonts
  "https://fonts.googleapis.com/css?family=Lato:300,400,700,900")

(def font-awesome
  "https://maxcdn.bootstrapcdn.com/font-awesome/4.6.3/css/font-awesome.min.css")

(html/defsnippet loading-fs "templates/partials/loading-fs.html" [:section] [])

(html/defsnippet app-navbar "templates/partials/app/navbar.html" [:header] [])

(html/defsnippet header "templates/partials/header.html" [:header]
  [& [active]]
  [:.nav-item] (maybe-activate active))

;; =============================================================================
;; Templates
;; =============================================================================

(deftemplate public-base "templates/base.html"
  [req & {:keys [header svg main fonts js-bundles]
          :or   {fonts      [default-fonts]
                 js-bundles ["main.js"]}}]
  [:head] (html/do->
           (html/append (html/html (css-bundles* req "public.css")))
           (html/append (html/html (fonts* fonts))))
  [:body] (html/do->
           (maybe-prepend svg)
           (html/append (html/html (js-bundles* req js-bundles))))
  [:header] (html/substitute (or header (starcity.views.base/header)))
  [:main] (maybe-substitute main)
  [:img] #(update-in % [:attrs :src] (optify req "/assets/img/")))

(deftemplate app-base "templates/app.html"
  [req app-name & {:keys [stylesheets navbar json scripts content fonts]
                   :or   {fonts [default-fonts]}}]
  [:head :title] (html/content (str "Starcity - " (string/capitalize app-name)))
  [:head] (html/do->
           (html/append (html/html (apply css stylesheets)))
           (html/append (html/html (css-bundles* req "styles.css")))
           (html/append (html/html (fonts* fonts))))
  [:body] (html/do->
           (maybe-prepend navbar)
           (maybe-append (html/html (json* json)))
           (html/append
            (html/html
             (concat
              (js* scripts)
              (js-bundles* req [(str app-name ".js")])
              [[:script (format "window.onload=function(){%s.core.run();}" app-name)]]))))
  [:#app] (if content
            (html/substitute content)
            (html/set-attr :id app-name))
  [:img] #(update-in % [:attrs :src] (optify req "/assets/img/")))
