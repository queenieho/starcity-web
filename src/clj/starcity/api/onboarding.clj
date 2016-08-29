(ns starcity.api.onboarding
  (:require [compojure.core :refer [context defroutes GET POST]]
            [starcity.api.common :refer :all]))

(defroutes routes
  (GET "/step" [] (fn [req] (ok {:step      "begin"
                                :community "Gilbert Street"}))))
