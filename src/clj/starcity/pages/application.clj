(ns starcity.pages.application
  (:require [starcity.pages.base :refer [base]]))

;; =============================================================================
;; Components

(def ^:private sections
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

(defn- page [req]
  [:div.container
   [:div.page-header
    [:h1 "Starcity Rental Application"]]

   (map section sections)

   ])

;; =============================================================================
;; API

(defn render [req]
  (let [username (get-in req [:session :identity :account/email])]
    (base
     (page req)
                                        ;:js ["app/main.js"]
     ;; :css ["forms.css"]
                                        ;:cljs-devtools? (= :development environment)
     )))
