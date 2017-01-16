(ns mars.menu.views
  (:require [re-frame.core :refer [subscribe dispatch]]
            [mars.components.antd :as a]
            [starcity.log :as l]))

(defmulti menu-item :type)

(defmethod menu-item :item [{:keys [label key]}]
  [a/menu-item {:key key} label])

(defmethod menu-item :submenu [{:keys [children title key]}]
  [a/sub-menu {:key key :title title}
   (map menu-item children)])

(defn- menu-items []
  (let [active (subscribe [:menu/active])
        open   (subscribe [:menu/open-submenus])
        items  (subscribe [:menu/items])]
    (fn []
      [a/menu {:mode           "inline"
               :selected-keys  [@active]
               :open-keys      @open
               :on-open-change #(dispatch [:menu.submenu/change-open (js->clj %)])
               :on-select      #(dispatch [:menu/select (.-key %)])}
       (doall (map menu-item @items))])))

(defn menu []
  [:div.column.mars-menu {:style {:padding-bottom 0 :padding-right 0 }}
   [:div.mars-menu__logo
    [:img {:src "/assets/img/starcity-logo-black.png"}]
    [:p "Starcity"]]
   [menu-items]])
