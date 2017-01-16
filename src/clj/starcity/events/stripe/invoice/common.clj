(ns starcity.events.stripe.invoice.common
  (:require [starcity.models
             [member-license :as member-license]
             [property :as property]
             [unit :as unit]]))

(defn managed-account
  [license]
  (-> license
      member-license/unit
      unit/property
      property/managed-account-id))

(defn dashboard-url [managed-id invoice-id]
  (format "https://dashboard.stripe.com/%s/invoices/%s"
          managed-id invoice-id) )
