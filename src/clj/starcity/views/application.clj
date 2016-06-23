(ns starcity.views.application
  (:require [starcity.views.base :refer [base]]))

;; =============================================================================
;; Helpers
;; =============================================================================

(def ^:private +sections+
  [{:title       "Logistics"
    :uri         "logistics"
    :description "Your desired move-in date, etc."}
   {:title "Background &amp; Credit Checks"
    :uri   "checks"}
   {:title "Community Fitness"
    :uri   "community"}
   {:title "Final Steps"
    :uri   "finalization"}])

(defn- section [{:keys [title uri description]
                 :or   {description "Aenean in sem ac leo mollis blandit."}}]
  [:div
   [:h3 [:a {:href (format "/application/%s" uri)} title]]
   [:p description]])

;; =============================================================================
;; API
;; =============================================================================

(defn application
  []
  (base
   [:div.container
    [:div.page-header
     [:h1 "Starcity Rental Application"]]
    (map section +sections+)]))
