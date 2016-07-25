(ns starcity.views.availability
  (:require [starcity.views.base :refer [base]]))

;; =============================================================================
;; Helpers
;; =============================================================================

;; (def ^:private header
;;   (common/header
;;    "52 Gilbert St."
;;    "Located in SoMa, this community has rooms available for move-in on September 1st."
;;    "/assets/img/gilbert-header.png"
;;    {:uri "/signup" :text "Apply Now"}))

;; (def ^:private rows
;;   [(common/featurette-left
;;     :image {:url "/assets/img/gilbert-kitchen-modern.png"}
;;     :copy {:title "The Space"
;;            :description "A Victorian house designed to be welcoming and enjoyable,
;;           this home offers members over 900 sq. ft. in shared community space.
;;           Members share a beautiful kitchen, dining area, media lounge, and laundry room. Each member has their own private bedroom to nest in. The entire home is
;;           furnished to make moving in hassle-free."})
;;    (common/featurette-right
;;     :image {:url "/assets/img/gilbert-media-room.png"}
;;     :copy {:title "Amenities"
;;            :description "Netflix? Check. On-site laundry? Yes. High-speed WiFi? Of course. Our
;;           members deserve the best. We offer an unparalleled package of
;;           amenities that includes bike storage, tea and coffee, weekly cleaning by a professional service, and access to a house concierge."})
;;    (common/featurette-left
;;     :image {:url "/assets/img/gilbert-bedroom-two.jpg"}
;;     :copy {:title "Flexible Terms"
;;            :description "We'd love for you to stay as long as San Francisco is
;;            your home. With that in mind, we allow you to choose the flexibility of staying month-to-month or committing to multi-month terms. We bundle all utilities and amenities into one flat price. Our all-inclusive price for a 12-month stay is $1,900 per month."})
;;    (common/featurette-right
;;     :image {:url "/assets/img/southpark-soma.jpg"}
;;     :copy {:title "Neighborhood"
;;            :description "SoMa's multi-faceted character brings together all
;;            sorts of people. The area has hip cafes, bars, and several museums, including the SF Museum of Modern Art. It is also home to the San Francisco Giants. <br><br> This Starcity community has great access to public transit. Bus lines (SF Muni), including the 19, 47, 27 and 8 are easy to access. The nearest BART station is only a 12-minute bus ride or 20-minute walk away."})])

;; =============================================================================
;; API
;; =============================================================================

(defn availability
  [units-available]
  (base
   :content [:main
             ;; header
             [:div.container.marketing
              ;; (for [row rows]
              ;;   [:div row
              ;;    (when (not= row (last rows))
              ;;      [:hr.featurette-divider])])
              [:div.row
               [:div.col-sm-4.col-sm-offset-4.col-xs-6.col-xs-offset-3
                [:h2#action-heading.text-center
                 {:style "margin-bottom: 40px;"}
                 "Interested?"]
                [:a.btn.btn-attention.btn-lg.btn-block
                 {:href "/signup" :style "margin-bottom: 80px;"}
                 "Apply Now"]]]]]))
