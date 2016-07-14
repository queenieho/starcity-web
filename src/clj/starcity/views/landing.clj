(ns starcity.views.landing
  (:require [starcity.views.base :refer [base]]
            [starcity.views.common :as common]))

;; =============================================================================
;; Helpers
;; =============================================================================

(def ^:private header
  (common/header
   "Your New Home in San Francisco"
   "Comfortable, communal housing for our city's workforce."
   "/assets/img/sf_homes_1.jpeg"
   {:uri "/signup" :text "Apply for a Home"}))

;; =============================================================================
;; API
;; =============================================================================

(defn landing
  []
  (base
   [:div
    header
    [:div.container.marketing
     [:div.row.featurette
      [:div.col-sm-7
       [:h2.featurette-heading
        "Diverse, Resource-Efficient Communities. "
        [:span.text-muted ""]]
       [:p.lead "Hard-working San Franciscans form the diverse fabric of this city. Yet we're often left out of the housing conversation. Let's change that. Let's build housing that allows us to thrive in our beloved city. Let's build communities that embrace individuals from all walks of life."]]
      [:div.col-sm-5
       [:img.featurette-image.img-responsive.img-rounded
        {:src "/assets/img/renderings/alpharendering.png"}]]]

     [:hr.featurette-divider]
     [:div.row.featurette
      [:div.col-sm-5
       [:img.featurette-image.img-responsive.img-rounded
        {:src "/assets/img/renderings/sf_rendering04.png"}]]
      [:div.col-sm-7
       [:h2.featurette-heading
        "Beautiful Private Spaces. <br> "
        [:span.text-muted "For Everyone."]]
       [:p.lead "Balancing community space and resources with adequate private space will honor the needs of our workforce. Let's design private rooms in a way that'll allow us to live sustainably in San Francisco."]]]

     [:hr#action-divider.featurette-divider]

     [:div#action-section.row
      [:div.col-md-6.col-md-offset-3
       [:h2#action-heading "Join Our Community"]
       [:p.lead "Enter your email to receive updates on Starcity's upcoming housing communities and to be invited to our public events."]

       [:form {:action "/register" :method "GET"}
        [:div.input-group.input-group-lg
         [:input.input.form-control
          {:type        "email"
           :name        "email"
           :required    true
           :placeholder "Enter your email address"}]
         [:span.input-group-btn
          [:button.btn.btn-primary "Join Us"]]]]]]]]
   :css ["landing.css"]))
