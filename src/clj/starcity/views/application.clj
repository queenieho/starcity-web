(ns starcity.views.application
  (:require [starcity.views.base :refer [base]]
            [starcity.views.base.nav :as nav]))

;; =============================================================================
;; Helpers
;; =============================================================================

(def ^:private next-steps
  ["We will process your application shortly &mdash; once we've pre-qualified you for the community, we'll reach out to schedule a tour and community-interview."])

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
     [:a {:href "mailto:team@joinstarcity.com"} "here"]
     " if you have any questions about your application."]]])

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
