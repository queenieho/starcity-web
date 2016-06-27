(ns starcity.views.register
  (:require [starcity.views.base :refer [base]]))

;; TODO: The navbar has been repeated a few times...
(def ^:private navbar
  [:nav.navbar
   [:div.container
    [:div.navbar-header
     [:a.navbar-brand {:href "#"}
      [:img {:alt "Starcity" :src "/assets/img/starcity-brand-icon-white.png"}]]]]])

;; =============================================================================
;; API
;; =============================================================================

(defn post-registration
  [email]
  (base
   [:div.navbar-wrapper
    [:div.container-fluid
     navbar
     [:div.container
      [:h3
       ;; TODO: Clean up this HTML...<br> is bad.
       (str (format "Thank you for getting involved, %s!" email)
            " With your help, we'll design and build community-focused housing
      that will keep San Francisco's diversity right here, in our city. <br>
      <br> Let's work together to make San Francisco a financially attainable
      place to live by adding new housing supply to our city. Be on the lookout
      for an email with more information on how to participate.")]]]]
   :css ["register.css"]))
