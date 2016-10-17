(ns starcity.views.legal.privacy
  (:require [starcity.views.page :as p]
            [starcity.views.legal.privacy.content :as content]))

;; =============================================================================
;; Content
;; =============================================================================

(def ^:private content
  [:section.section
   [:div.container
    [:h1.title.is-2 "Privacy Policy"]
    [:h2.subtitle.is-4 "Starcity Properties, Inc"]
    [:hr]
    [:div.content
     [:p content/preamble]
     [:ol content/privacy]
     [:p [:small "Effective Date of Privacy Policy: July 4, 2016"]]]]])

;; =============================================================================
;; API
;; =============================================================================

(def privacy
  (p/page
   (p/title "Privacy Policy")
   p/navbar
   content))
