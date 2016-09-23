(ns starcity.api.apply
  (:require [compojure.core :refer [context defroutes GET POST]]
            [starcity.api.common :refer :all]))

;; /api/v1/apply/...
(defroutes routes
  (POST "/update" [] (fn [req] (ok {:message "Success!"})))

  (POST "/verify-income" [] (fn [req] (ok {:message "Success!"})))
  )
