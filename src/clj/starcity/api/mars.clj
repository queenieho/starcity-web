(ns starcity.api.mars
  (:require [compojure.core :refer [context defroutes GET POST]]
            [starcity.api.mars.rent :as rent]
            [starcity.api.mars.news :as news]
            [starcity.api.mars.settings :as settings]
            [starcity.api.mars.security-deposit :as security-deposit]
            [starcity.api.common :refer :all]))

(defroutes routes
  (context "/news" [] news/routes)
  (context "/rent" [] rent/routes)
  (context "/security-deposit" [] security-deposit/routes)
  (context "/settings" [] settings/routes))
