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
   [:p.flow-text-small blurb]])

(def ^:private landing-content
  [:div

   [:section.features
    [:div.container
     [:div.row
      [:div.center-align
       [:h3 "What's Included"]
       [:p.flow-text "Here's what we offer to members that join any of our communities:"]]
      (feature "Private &amp; Secure" "/assets/img/promo-bar-privacy.png"
               "TODO:")

      (feature "Flexible Terms" "/assets/img/promo-bar-community.png"
               "TODO:")

      (feature "Amenities" "/assets/img/promo-bar-amenities.png"
               "TODO:")

      ]]]

   [:section.community.grey.valign-wrapper
    [:div.container
     [:div.row
      [:div.col.s12.center-align.white-text
       [:h3 "Community"]
       [:p.flow-text-large "Community-building in this city must reflect the eclectic nature of San Francisco itself. We are looking for members that embrace individuals from all walks of life."]
       ;; [:p.flow-text "We are building communities that embrace individuals from all walks of life."]
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
         [:button.btn.waves-effect.waves-light.btn-large {:type "submit"}
          "Join Us"
          [:i.material-icons.right "send"]]]]]]]]])

(defn landing []
  (hero
   :content landing-content
   :title "Your new home in san francisco"
   :description "Comfortable, communal housing for our city's workforce."
   :background-image "/assets/img/sf_homes_1.jpeg"
   :action {:uri   "/application"
            :text  "apply for a home"
            :class "star-orange"}))
