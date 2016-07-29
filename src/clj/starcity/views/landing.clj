(ns starcity.views.landing
  (:require [starcity.views.base :refer [hero]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-css include-js]]
            [clojure.string :refer [lower-case]]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- promo-section
  ([title text img-url]
   (promo-section title text img-url :right))
  ([title text img-url img-orientation]
   (let [lcontent (if (= img-orientation :right)
                    [:div.col.m7
                     [:h3 title]
                     [:p.flow-text text]]
                    [:div.col.m5
                     [:img.responsive-img {:src img-url}]])
         rcontent (if (= img-orientation :right)
                    [:div.col.m5
                     [:img.responsive-img {:src img-url}]]
                    [:div.col.m7
                     [:h3 title]
                     [:p.flow-text text]])]
     [:section
      [:div.row.valign-wrapper.hide-on-small-only
       lcontent rcontent]

      [:div.row.hide-on-med-and-up
       [:div.col.s12
        [:h3 title]
        [:p.flow-text text]]

       [:div.col.s12
        [:img.responsive-img {:src img-url}]]]

      [:div.divider]])))

;; =============================================================================
;; API
;; =============================================================================

;; TODO: rm -r resources/public/js/bower

(defn- feature
  [title img-src blurb]
  [:div.col.m8.offset-m2.l4.center-align
   [:img.circle.responsive-img {:src img-src}]
   [:h5.center title]
   [:p.light blurb]])

(def ^:private landing-content
  [:div

   [:section.features
    [:div.container
     [:div.row
      [:div.center-align
       [:h3 "Member Benefits"]
       [:p.flow-text "All of our communities offer the following:"]
       ]
      (feature "Privacy &amp; Security" "/assets/img/promo-bar-privacy.png"
               "Each member receives a secure, private room to nest in and make their own. All members run through a background check to ensure community safety.")

      (feature "Community" "/assets/img/promo-bar-community.png"
               "Our buildings offer plenty of shared space for everyday interaction with fellow community members and guests. Kitchens, dining areas, and entertainment rooms bring everyone together.")

      (feature "Comfort" "/assets/img/promo-bar-amenities.png"
               "Fully-furnished? Yes. On-site laundry? Check. Hi-speed WiFi? Of course. Amenities are included to make your life simple.")

      ]]]

   [:section.community.grey.valign-wrapper
    [:div.container
     [:div.row
      [:div.col.s12.center-align.white-text
       [:h3 "A Focus on Community"]
       [:p.flow-text-large "Our homes are welcoming, relaxing and safe. Our members are respectful, warm and empathetic people that come together to form inclusive, uplifting communities. We are seeking new members that reflect the eclectic nature of San Francisco itself, embracing individuals from all walks of life."]
       ]]]]

   ;; memberships from 1 month to 1 year...

   [:section
    [:div.container
     [:div.row
      [:form.col.m8.offset-m2.s12 {:action "/register" :method "GET"}
       [:div.row
        [:h3.center-align "Join Us"]
        [:p.flow-text.center "Enter your email to receive updates on Starcity's upcoming housing communities and events."]]
       [:div.row
        [:div.input-field
         [:input#email.validate {:type "email" :required true}]
         [:label {:for "email"} "Email"]]]
       [:div.row
        [:div.col.s12.center-align
         [:button.btn.waves-effect.waves-light.btn-large.star-green.lighten-1 {:type "submit"}
          "Join Us"
          [:i.material-icons.right "send"]]]]]]]]])

(defn landing []
  (hero
   :content landing-content
   :title "Your new home in san francisco"
   :description "Comfortable, communal housing for our city's workforce."
   :background-image "/assets/img/starcity-kitchen.png"
   :action {:uri   "/application"
            :text  "apply for a home"
            :class "star-orange"}))
