(ns starcity.views.communities
  (:require [starcity.views.base :refer [base]]
            [clojure.spec :as s]
            [clj-time.format :as f]
            [clj-time.core :as t]
            [clj-time.coerce :as c]))

;; =============================================================================
;; Helpers
;; =============================================================================

(def ^:private month-day-year-formatter (f/formatter "MMMM d, y"))

;; =============================================================================
;; Content
;; =============================================================================

(defn- card-img-gradient
  [url]
  (format "background-image: linear-gradient(rgba(0,0,0,0), rgba(0,0,0,0.5)), url('%s');" url))

(defn- membership-rate-text
  [{:keys [:property-license/base-price :property-license/license]}]
  (let [term (:license/term license)]
    (if (> term 1)
      (format "$%.0f/month for %s months" base-price term)
      (format "$%.0f/month for month-to-month" base-price))))

(defn- property-card
  [{:keys [:db/id
           :property/name
           :property/description
           :property/internal-name
           :property/upcoming
           :property/available-on
           :property/cover-image-url
           :property/licenses]}]
  (let [starting-price (-> (sort-by :property-license/base-price licenses)
                           first
                           :property-license/base-price)]
    [:div.card.sticky-action
     [:div.card-image.waves-effect.waves-block.waves-light
      [:img.gradient.activator {:style (card-img-gradient cover-image-url)}]
      [:span.card-title.activator name]]
     [:div.card-content
      [:i.activator.material-icons.right "more_vert"]
      (if-not upcoming
        (list
         [:p (str "Available " (->> available-on c/from-date (f/unparse month-day-year-formatter)))]
         [:p.light (format "From $%.0f/month" starting-price)])
        [:p upcoming])]
     [:div.card-reveal
      [:span.card-title name
       [:i.material-icons.right "close"]]
      [:p description]
      (when-not upcoming
        (list
         [:span.card-subtitle "Membership Rates"]
         [:ul
          (->> licenses
               (sort-by :property-license/base-price <)
               (map (fn [property-license]
                      [:li (membership-rate-text property-license)])))]))]]))

(defn- content
  [properties]
  [:main
   [:div.container
    [:h2 "Our Communities"]
    [:p.flow-text "Here's a catalog of homes you can apply to live in. Each community has a unique character and layout. Explore them below to determine which appeal to you."]
    [:div.row
     (for [p (sort-by :property/available-on properties)]
       [:div.col.l6.m12.s12
        (property-card p)])]]])

;; =============================================================================
;; API
;; =============================================================================

(s/def ::property (s/keys :req [:db/id
                                :property/name
                                :property/description
                                :property/available-on
                                :property/internal-name
                                :property/cover-image-url
                                :property/licenses])) ; TODO: Spec these

(defn communities
  [properties]
  (base
   :title "Communities"
   :content (content properties)))

(s/fdef communities
        :args (s/cat :properties (s/spec (s/+ ::property)))
        :ret  string?)
