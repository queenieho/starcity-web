(ns starcity.views.application
  (:require [starcity.views.application.common :as common]
            [starcity.views.base :refer [base]]))

;; =============================================================================
;; Helpers
;; =============================================================================

(def ^:private next-steps
  ["We will process your application shortly &mdash; it should take 1-2 business days to pre-qualify you for the community."
   "You will receive an email with instructions to schedule a tour."])

(def ^:private just-completed-content
  [:div.container
   [:div.page-header
    [:h1 "Thanks for submitting your application!"]]
   [:h4 "Here's what will happen next:"]
   [:ul
    (map (fn [text] [:li text]) next-steps)]])

(def ^:private post-completion-content
  [:div.container
   [:div.page-header
    [:h1 "Application Status"]]
   [:p.lead "Your application is under review &mdash; please reach out to us "
    [:a {:href "mailto:team@starcityproperties.com"} "here"]
    " if you have any questions about your application."]])

;; =============================================================================
;; API
;; =============================================================================

(defn locked                            ; TODO: better naming
  [just-completed]
  (base
   (if just-completed
     just-completed-content
     post-completion-content)))

(defn application
  [current-steps]
  (let [active (common/active-step current-steps)]
    (common/application
     current-steps
     [:div.row
      [:div.col-xs-12
       [:a.btn.btn-lg.btn-success
        {:href (common/uri-for-step active)} (if (= active :logistics)
                                               "Start Now"
                                               "Resume")]]])))
