(ns mars.db
  (:require [mars.menu.db :as menu]
            [mars.activity.db :as activity]
            [mars.account.db :as account]))

(def path ::mars)
(def default-value
  (merge {path {:route   :init
                :scripts {}}}
         account/default-value
         menu/default-value
         activity/default-value))
