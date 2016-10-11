(ns starcity.views.legal.terms
  (:require [starcity.views.page :as p]
            [starcity.views.legal.terms.content :as content]))

;; =============================================================================
;; Content
;; =============================================================================

(def ^:private content
  [:section.section
   [:div.container
    [:h1.title.is-2 "Terms of Service"]
    [:h2.subtitle.is-4 "Starcity Properties, Inc"]
    [:hr]
    [:div.content
     [:p content/preamble]
     [:ol content/terms]
     [:p [:small "Effective Date of Terms of Service: July 14, 2016"]]]]])

;; =============================================================================
;; API
;; =============================================================================

(def terms
  (p/page
   (p/title "Terms of Service")
   (p/content
    (p/navbar)
    content)))
