(ns starcity.services.mailchimp
  (:require [mount.core :as mount :refer [defstate]]
            [starcity.config :refer [config]]))

;; (defstate mailchimp-request
;;   :start (create-mailchimp-request (:mailchimp config)))
