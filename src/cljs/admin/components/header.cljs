(ns admin.components.header
  (:require [ant-ui.core :as a]
            [admin.routes :as routes]
            [re-frame.core :refer [subscribe]]))

(defn- breadcrumbs []
  (let [definitions (subscribe [:nav/breadcrumbs])]
    (fn []
      [a/breadcrumb
       (doall
        (for [[link label] @definitions]
          ^{:key link} [a/breadcrumb-item [:a {:href link} label]]))])))

(defn header [& [right]]
  (let [right (or right [:div])]
    [a/layout-header {:id "admin-content-header"}
     [:div.columns
      [:div.header-left.column
       [breadcrumbs]]
      [:div.header-right.column
       right]]]))
