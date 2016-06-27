(ns starcity.views.landing
  (:require [starcity.views.base :refer [base]]))

;; =============================================================================
;; Helpers
;; =============================================================================

;; =============================================================================
;; API
;; =============================================================================

(defn landing
  []
  (base
   [:div
    [:div.header
     [:div.header-inner
      [:div.header-content
       [:div.container
        [:h1 "reimagine home in San Francisco"]
        [:p.lead "Help shape community-focused housing for our city's workforce."]
        [:div.row
         [:div.col-sm-4.col-sm-offset-4.col-xs-8.col-xs-offset-2
          [:a#action-button.btn.btn-lg.btn-block.btn-default
           {:href "/application"}
           "Join Our Community"]]]]]]]

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

     [:hr#informed-divider.featurette-divider]

     [:div#informed-section.row
      [:div.col-md-6.col-md-offset-3
       [:h2#informed-heading "Get Involved"]
       [:p.lead "Enter your email and we'll be in touch."]

       [:form {:action "/register" :method "GET"}
        [:div.input-group.input-group-lg
         [:input.input.form-control
          {:type        "email"
           :name        "email"
           :required    true
           :placeholder "Enter your email address"}]
         [:span.input-group-btn
          [:button.btn.btn-primary "Join Us"]]]]]]]]
   :css ["landing.css"]
   :nav-items [["Availability" "/availability"]
               ["FAQ" "/fack"]]
   :nav-buttons [["Apply Now" "/application" {:class "btn-attention navbar-right"}]]))
