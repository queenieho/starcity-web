(ns starcity.api.mars
  (:require [compojure.core :refer [context defroutes GET POST]]
            [starcity.api.mars.rent :as rent]
            [starcity.api.mars.news :as news]
            [starcity.api.common :refer :all]))

(defroutes routes
  (GET "/init" [] (fn [req] (ok {:message "hi!"})))

  (context "/rent" [] rent/routes)
  (context "/news" [] news/routes))
