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

(def ^:private landing-content
  [:div.container

    [:section.features
     [:div.row
      [:div.col.s4.center-align
       [:img.circle.responsive-img {:src "/assets/img/promo-bar-community.png"}]
       [:h5.center "Community"]
       [:p.light "Diverse people, yada yada"]]
      [:div.col.s4.center-align
       [:img.circle.responsive-img {:src "/assets/img/promo-bar-amenities.png"}]
       [:h5.center "Amenities"]
       [:p.light "Diverse people, yada yada"]]
      [:div.col.s4.center-align
       [:img.circle.responsive-img {:src "/assets/img/promo-bar-privacy.png"}]
       [:h5.center "Privacy"]
       [:p.light "Diverse people, yada yada"]]]]

    ;; (promo-section
    ;;  "Diverse, Resource-Efficient Communities."
    ;;  "Hard-working San Franciscans form the diverse fabric of this city. Yet we're often left out of the housing conversation. Let's change that. Let's build housing that allows us to thrive in our beloved city. Let's build communities that embrace individuals from all walks of life."
    ;;  "/assets/img/renderings/alpharendering.png"
    ;;  :right)
    ;; (promo-section
    ;;  "Beautiful Private Spaces. For Everyone."
    ;;  "Balancing community space and resources with adequate private space will honor the needs of our workforce. Let's design private rooms in a way that'll allow us to live sustainably in San Francisco."
    ;;  "/assets/img/renderings/sf_rendering04.png"
    ;;  :left)

    [:section
     [:div.row
      [:form.col.m8.offset-m2.s12 {:action "/register" :method "GET"}
       [:div.row
        [:h4.center-align "Join Our Community"]
        [:p.flow-text.center "Enter your email to receive updates on Starcity's upcoming housing communities and to be invited to our public events."]]
       [:div.row
        [:div.input-field
         [:input#email.validate {:type "email" :required true}]
         [:label {:for "email"} "Email"]]]
       [:div.row
        [:div.col.s12.center-align
         [:button.btn.waves-effect.waves-light.btn-large {:type "submit"}
          "Join Us"
          [:i.material-icons.right "send"]]]]]]]])

(defn landing []
  (hero
   :content landing-content
   :title "Your new home in san francisco"
   :description "Comfortable, communal housing for our city's workforce."
   :background-image "/assets/img/sf_homes_1.jpeg"
   :action {:uri   "/application"
            :text  "apply for a home"
            :class "star-orange"}))
