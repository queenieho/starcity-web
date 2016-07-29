(ns starcity.views.legal.render
  (:require [hiccup.core :refer [html]]
            [clojure.string :refer [replace]])
  (:refer-clojure :exclude [replace]))

(declare render-term)

;; =============================================================================
;; API
;; =============================================================================

(defprotocol ContentItem
  "An item of content within the legal document content."
  (render [content] "Render a content item as Hiccup."))

(defmulti render-term-content :type)

;; =============================================================================
;; Term Content Constructors

(defn with-title
  "Constructor for a new term."
  [title term]
  [title term])

(defn list-content
  [& content]
  (let [m {:type    :list
           :content content}]
    (if (keyword? (first content))
      (assoc m :list-style (first content) :content (rest content))
      m)))

(defn paragraphs-list
  [& content]
  {:type    :paragraphs
   :content content})

(defn lead-list-content
  [& content]
  {:type       :lead-list
   :content    content})

(def text identity)

(defn content
  [content]
  {:content content})

(def ^:private default-substitutions
  {"team@starcityproperties.com" [:a {:href "mailto:team@starcityproperties.com"} "team@starcityproperties.com"]
   "Plaid"                       [:a {:href "https://plaid.com/legal/#plaid-services"} "Plaid"]})

(defn- make-substitutions
  [terms-html substitutions]
  (reduce (fn [acc [match replacement]]
            (replace acc match (html replacement)))
          terms-html
          (merge default-substitutions substitutions)))

(defn render-terms
  ([terms]
   (render-terms terms {}))
  ([terms substitutions]
   (let [terms-html (html (map render-term terms))]
     (make-substitutions terms-html substitutions))))

;; =============================================================================
;; Private
;; =============================================================================

(def ^:private list-styles
  {:numeric      "1"
   :roman        "i"
   :alphabetical "a"})

(defmethod render-term-content :list
  [{:keys [content list-style]}]
  [:ol {:type (get list-styles list-style :numeric)}
   (map render content)])

(defmethod render-term-content :paragraphs
  [{content :content}]
  (list
   [:span (first content)]
   (for [p (rest content)]
     [:p p])))

(defmethod render-term-content :lead-list
  [{content :content}]
  (list
   [:span (first content)]
   [:ol {:type "a"}
    (map render (rest content))]))

(defmethod render-term-content nil
  [{content :content}]
  [:span (str " " content)])

(extend-type java.lang.String
  ContentItem
  (render [content]
    [:li content]))

(extend-type clojure.lang.PersistentVector
  ContentItem
  (render [[definition content]]
    [:li
     [:u definition]
     [:span ": " content]]))

(extend-type clojure.lang.PersistentArrayMap
  ContentItem
  (render [item]
    (render-term-content item)))

(defn- render-term [[title content]]
  [:li [:strong (str title ": ")] (render-term-content content)])
