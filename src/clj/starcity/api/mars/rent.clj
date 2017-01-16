(ns starcity.api.mars.rent
  (:require [compojure.core :refer [context defroutes]]
            [starcity.api.mars.rent
             [autopay :as autopay]
             [bank-account :as bank-account]
             [payments :as payments]]))

(defroutes routes
  (context "/bank-account" [] bank-account/routes)
  (context "/payments" [] payments/routes)
  (context "/autopay" [] autopay/routes))
