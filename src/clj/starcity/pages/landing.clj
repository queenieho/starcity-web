(ns starcity.pages.landing
  (:require [starcity.pages.base :refer [base]]
            [starcity.pages.util :refer [ok]]))

;; =============================================================================
;; Constants


;; =============================================================================
;; Components

(defn- landing-view []
  (base
   [:div#app
    [:h1 "Hello, Starcity!"]]))

;; =============================================================================
;; API

(defn handle [req]
  (ok (landing-view)))
