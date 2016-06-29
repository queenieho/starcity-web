(ns starcity.views.common
  (:require [clojure.spec :as s]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- background-style
  [url]
  {:style (format "background: linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url('%s'); background-size: cover; -webkit-background-size: cover; -moz-background-size: cover; -o-background-size: cover; background-repeat: no-repeat;" url)})

;; =============================================================================
;; API
;; =============================================================================

(defn header
  [title subtitle background-image-url {:keys [text uri]}]
  [:div.header (background-style background-image-url)
   [:div.header-inner
    [:div.header-content
     [:div.container
      [:h1 title]
      [:p.lead subtitle]
      [:div.row
       [:div.col-sm-4.col-sm-offset-4.col-xs-8.col-xs-offset-2
        [:a#action-button.btn.btn-lg.btn-block.btn-default {:href uri} text]]]]]]])

(defn placeholder-image-url
  [width height]
  (format "http://placehold.it/%sx%s" width height))

;; =============================================================================
;; Featurette

(defn- featurette-image-col
  [{:keys [url]}]
  (let [image-url (or url (placeholder-image-url 450 400))]
    [:div.col-sm-5
     [:img.featurette-image.img-responsive.img-rounded {:src image-url}]]))

(defn- featurette-copy-col
  [{:keys [title description]}]
  [:div.col-sm-7
   [:h2.featurette-heading title]
   (when description
     [:p.lead description])])

(defn featurette-left
  "Featurette column with image oriented to the left."
  [& {:keys [image copy]}]
  [:div.row.featurette
   (featurette-image-col image)
   (featurette-copy-col copy)])

(defn featurette-right
  "Featurette column with image oriented to the right."
  [& {:keys [image copy]}]
  [:div.row.featurette
   (featurette-copy-col copy)
   (featurette-image-col image)])

(comment
  ;; TODO:
  (s/fdef featurette
          :args (s/cat :orientation #{:left :right}))
  )
