(ns starcity.views.application
  (:require [starcity.views.base :refer [base]]))

;; =============================================================================
;; Helpers
;; =============================================================================

(def ^:private +sections+
  [{:section     :logistics
    :title       "Logistics"
    :uri         "logistics"
    :description "Your desired move-in date, etc."}
   {:section :checks
    :title   "Background &amp; Credit Checks"
    :uri     "checks"}
   {:section :community
    :title   "Community Fitness"
    :uri     "community"}])

(defn- section
  [allowed-sections {:keys [title uri description section]}] ; dev
  (let [allowed? (allowed-sections section)]
    [:div
     [:h3
      (if allowed?
        [:a {:href (format "/application/%s" uri)} title]
        title)]
     [:p (or description "Aenean in sem ac leo mollis blandit.")]]))

;; =============================================================================
;; API
;; =============================================================================

(defn application
  [allowed-sections]
  (base
   [:div.container
    [:div.page-header
     [:h1 "Starcity Rental Application"]]
    (map (partial section allowed-sections) +sections+)]
   :nav-buttons []
   :nav-items []))
