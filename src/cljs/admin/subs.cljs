(ns admin.subs
  (:require [admin.application.subs]
            [admin.applications.subs]
            [re-frame.core :refer [reg-sub]]))

(reg-sub
 :app/current-route
 (fn [db _]
   (:route db)))
