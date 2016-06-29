(ns starcity.views.availability
  (:require [starcity.views.base :refer [base]]
            [starcity.views.common :as common]))

;; =============================================================================
;; Helpers
;; =============================================================================

(def ^:private header
  (common/header
   "52 Gilbert St."
   "Located in SOMA, this community will have rooms available for move-in on September 1st."
   "/assets/img/gilbert-header.png"
   {:uri "/signup" :text "Apply Now"}))

(def ^:private rows
  [(common/featurette-left
    :copy {:title "The Space"
           :description "A victorian home designed to be welcoming and enjoyable,
          Gilbert offers members over 900 square feet in Community space.
          Members share a beautiful kitchen, lounge, media room and more. Each
          member has their own private bedroom to nest in. The entire home is
          furnished to make moving in hassle-free."})
   (common/featurette-right
    :copy {:title "Amenities"
           :description "On-site laundry? Check. High-speed WiFi? Yes. Our
          members deserve the best. We offer an unparalleled package of
          amenities that includes a weekly cleaning service, good coffee and
          more."})
   (common/featurette-left
    :copy {:title "Flexible Pricing"
           :description "We'd love for you to stay as long as San Francisco is
           your home. With that in mind, we allow you to choose stays ranging
           from month-to-month to 12 months. Our all-inclusive price for 12
           months is $1,900 per month."})
   (common/featurette-right
    :copy {:title "Neighborhood"
           :description "SOMA's multi-faceted character brings together all
           sorts of people..."})])

;; =============================================================================
;; API
;; =============================================================================

(defn availability
  [units-available]
  (base
   [:div
    header
    [:div.container.marketing
     (for [row rows]
       [:div row
        (when (not= row (last rows))
          [:hr.featurette-divider])])
     [:hr#action-divider.featurette-divider]
     [:div.row
      [:div.col-sm-4.col-sm-offset-4.col-xs-6.col-xs-offset-3
       [:h2#action-heading.text-center
        {:style "margin-bottom: 40px;"}
        "Interested?"]
       [:a.btn.btn-attention.btn-lg.btn-block
        {:href "/signup" :style "margin-bottom: 80px;"}
        "Apply Now"]]]]]
   :css ["landing.css"]))
