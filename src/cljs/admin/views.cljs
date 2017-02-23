(ns admin.views
  (:require [admin.content :refer [app-content]]
            [admin.accounts.views]
            [admin.properties.views]
            [admin.units.views]
            [ant-ui.core :as a]
            [re-frame.core :refer [dispatch subscribe]]))

(defn- menu-item [{:keys [key text icon]}]
  [a/menu-item {:key key}
   (when icon [a/icon {:type icon}])
   [:span.nav-text text]])

(defn- menu []
  (let [items    (subscribe [:menu/items])
        selected (subscribe [:menu/selected-item])]
    (fn []
      [a/menu {:theme         "dark"
               :mode          "inline"
               :selected-keys [@selected]
               :on-select     #(dispatch [:menu/item-selected (.-key %)])}
       (doall (map menu-item @items))])))

(defmethod app-content :default [{page :page}]
  [:h2 (str "page not found: " page)])

(defn app []
  (let [route (subscribe [:nav/route])]
    (fn []
      [a/layout {:class "ant-layout-has-sider" :style {:min-height "100vh"}}
       [a/layout-sider {:collapsible true}
        [:div.logo]
        [menu]]
       [app-content @route]])))
