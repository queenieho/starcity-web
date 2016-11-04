(ns starcity.fx
  (:require [re-frame.core :refer [reg-fx]]
            [accountant.core :as accountant]))

(reg-fx
 :route
 (fn [new-route]
   (accountant/navigate! new-route)))
