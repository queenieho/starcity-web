(ns starcity.pages.register
  (:require [starcity.pages.base :refer [base]]
            [starcity.pages.util :refer [ok]]
            [taoensso.timbre :refer [debug]]))

;; =============================================================================
;; Components

(defn- content [{:keys [params] :as req}]
  [:h3 (format "Prepare for spam, %s!" (:email params))])

(defn- view [req]
  (base (content req)
        :css ["landing.css"]            ; TODO:
        ))

;; =============================================================================
;; API

(defn handle [{:keys [params] :as req}]
  (debug "The params are:" params)
  (ok (view req)))
