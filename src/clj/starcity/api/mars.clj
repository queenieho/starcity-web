(ns starcity.api.mars
  (:require [compojure.core :refer [context defroutes]]
            [starcity.api.mars
             [news :as news]
             [rent :as rent]
             [security-deposit :as security-deposit]
             [settings :as settings]]))

(defroutes routes
  (context "/news" [] news/routes)
  (context "/rent" [] rent/routes)
  (context "/security-deposit" [] security-deposit/routes)
  (context "/settings" [] settings/routes))
