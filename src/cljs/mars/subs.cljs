(ns mars.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 :app/current-route
 (fn [db _]
   (:route db)))
