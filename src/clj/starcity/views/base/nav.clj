(ns starcity.views.base.nav
  (:require [clojure.string :refer [lower-case]])
  (:refer-clojure :exclude [apply]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- nav-link
  ([text]
   (nav-link text (format "/%s" (-> text lower-case))))
  ([text uri]
   [:li [:a {:href uri} text]]))

(defn- nav-button
  ([text]
   (nav-button text (format "/%s" (-> text lower-case))))
  ([text uri & classes]
   [:li
    [:a.waves-effect.waves-light.btn {:href uri :class (clojure.core/apply str classes)}
     text]]))

;; =============================================================================
;; API
;; =============================================================================

(def communities (nav-link "Our Communities" "/communities"))
(def faq (nav-link "FAQ" "/faq"))
(def about (nav-link "About"))
(def apply (nav-button "Apply Now" "/application" "star-orange"))
(def logout (nav-link "Log Out" "/logout"))
