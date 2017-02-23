(ns admin.accounts.views
  (:require [admin.accounts.views.common :refer [autocomplete]]
            [admin.accounts.views.entry :as entry]
            [admin.accounts.views.overview :as overview]
            [admin.components.header :as h]
            [admin.content :refer [app-content]]
            [ant-ui.core :as a]))

(defmethod app-content :accounts [_]
  [a/layout
   (h/header
    [:div {:style {:float "right"}} [autocomplete]])
   [overview/content]])

;; Entry
(defmethod app-content :account [_]
  [a/layout
   (h/header
    [:div {:style {:float "right"}}
     [autocomplete]])
   [entry/content]])
