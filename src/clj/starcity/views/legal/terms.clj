(ns starcity.views.legal.terms
  (:require [starcity.views.base :refer [base]]
            [starcity.views.legal.terms.content :as content]))

;; =============================================================================
;; Content
;; =============================================================================

(def ^:private content
  [:main#legal
   [:div.container
    [:div.center
     [:h3 "Starcity Properties, Inc"]
     [:h4 "Terms of Service"]]
    [:div.divider]
    [:p content/preamble]
    [:ol content/terms]
    [:p [:small "Effective Date of Terms of Service: July 14, 2016"]]]])

;; =============================================================================
;; API
;; =============================================================================

(defn terms
  [req]
  (base
   :req req
   :title "Terms of Service"
   :content content))
