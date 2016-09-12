(ns admin.applications.subs
  (:require [re-frame.core :refer [reg-sub]]
            [starcity.dates :as d]))

;; Ordered keys identifying values in application map
(reg-sub
 :applications/header-keys
 ;; TODO: Move to init
 (fn [db _]
   (get-in db [:applications :header-keys])))

(reg-sub
 :applications/list
 (fn [db _]
   (get-in db [:applications :list])))

(reg-sub
 :applications/sort
 (fn [db _]
   (get-in db [:applications :sort])))
