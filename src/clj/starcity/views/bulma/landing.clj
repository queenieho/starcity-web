(ns starcity.views.bulma.landing
  (:require [starcity.views.page :as p]
            [starcity.views.components
             [navbar :refer [navbar nav-item]]
             [hero :as hero]]
            [hiccup.core :refer [html]]
            [hiccup.element :refer [link-to mail-to unordered-list]]))

;; =============================================================================
;; Internal
;; =============================================================================

(defn- gradient-background-image
  [url]
  {:style (format "background-image: linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url('%s');" url) :class "has-background-image"})

;; =============================================================================
;; Components
;; =============================================================================

;; =============================================================================
;; Hero

(defn- auth-item [req]
  (let [role          (get-in req [:identity :account/role])
        [uri content] (case role
                        :account.role/applicant ["/application" "Resume Application"]
                        :account.role/pending   ["/onboarding" "Security Deposit"]
                        :account.role/admin     ["/admin" "Admin"]
                        ["/login" "Log In"])]
    (nav-item uri content :button)))

(defn- hero-head [req]
  (hero/head (p/navbar-inverse (auth-item req))))

(def ^:private hero-body
  (html
   (hero/body
    [:div.container
     [:h1.title.is-1 "Your new home."]
     [:h2.subtitle.is-4
      "Comfortable, communal housing in " [:strong "San Francisco"]]])))

(defn- hero-section [req]
  (let [attrs (merge (gradient-background-image "/assets/img/starcity-kitchen.png")
                     {:class "has-background-image is-fullheight is-primary"})]
    (hero/hero attrs (hero-head req) hero-body)))

;; =============================================================================
;; API
;; =============================================================================

(def landing
  (p/page "Starcity" (p/content hero-section)))
