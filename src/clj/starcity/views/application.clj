(ns starcity.views.application
  (:require [starcity.views.base :refer [base]]
            [starcity.views.base.nav :as nav]))

;; =============================================================================
;; Helpers
;; =============================================================================

(def ^:private next-steps
  ["We will process your application shortly &mdash; it should take 1-2 business days to pre-qualify you for the community."
   "Then, you will receive an email with instructions to schedule a tour."])

(def ^:private just-completed-content
  [:main
   [:div.container
    [:h3 "Thanks for submitting your application!"]
    [:div.divider]
    (map (fn [text] [:p.flow-text text]) next-steps)]])

(def ^:private post-completion-content
  [:main
   [:div.container
    [:h3 "Application Status"]
    [:div.divider]
    [:p.flow-text "Your application is under review &mdash; please reach out to us "
     [:a {:href "mailto:team@starcityproperties.com"} "here"]
     " if you have any questions about your application."]]])

(def ^:private get-started-content
  [:main
   [:div.container
    [:div.row.section
     [:div.card-panel
      [:h3 "Let's Get Started"]
      [:div.divider]
      [:p.flow-text "Integer placerat tristique nisl.  Etiam vel tortor sodales tellus ultricies commodo.  Vestibulum convallis, lorem a tempus semper, dui dui euismod elit, vitae placerat urna tortor vitae lacus.  Nullam eu ante vel est convallis dignissim.  Etiam vel neque nec dui dignissim bibendum.  Cras placerat accumsan nulla.  Vivamus id enim.  Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus.  Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus.  Donec vitae dolor.  Sed diam.  Sed bibendum."]
      [:div.row
       [:div.col.s12
        [:a.btn.waves-effect.waves-light.btn-large
         {:href "/application/logistics"}
         "Start"]]]]]]])

;; =============================================================================
;; API
;; =============================================================================

(defn locked
  [just-completed]
  (base
   :content (if just-completed
              just-completed-content
              post-completion-content)
   :nav-links [nav/logout]))

(defn application
  [current-steps]
  (base
   ;; TODO: Depends on current-steps
   :content get-started-content
   :nav-links [nav/logout]))
