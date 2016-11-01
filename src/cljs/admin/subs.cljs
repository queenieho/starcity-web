(ns admin.subs
  (:require [admin.application.entry.subs]
            [admin.application.list.subs]
            [admin.account.list.subs]
            [admin.account.entry.subs]
            [admin.notify.subs]
            [re-frame.core :refer [reg-sub]]))

(reg-sub
 :app/current-route
 (fn [db _]
   (:route db)))
