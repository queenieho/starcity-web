(ns starcity.api.dashboard.routes
  (:require [compojure.core :refer [context defroutes GET POST]]
            [starcity.api.common :refer :all]))

(defroutes routes
  (GET "/init" [] (fn [req] (ok {:message "hi!"}))))
