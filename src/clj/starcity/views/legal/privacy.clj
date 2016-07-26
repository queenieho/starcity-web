(ns starcity.views.legal.privacy
  (:require [starcity.views.base :refer [base]]
            [starcity.views.legal.privacy.content :as content]))

;; =============================================================================
;; Content
;; =============================================================================

(def ^:private content
  [:main#legal
   [:div.container
    [:div.center
     [:h3 "Starcity Properties, Inc"]
     [:h4 "Privacy Policy"]]
    [:div.divider]
    [:p content/preamble]
    [:ol content/privacy]
    [:p [:small "Effective Date of Privacy Policy: July 4, 2016"]]]])

;; =============================================================================
;; API
;; =============================================================================

(defn privacy
  []
  (base :content content))
