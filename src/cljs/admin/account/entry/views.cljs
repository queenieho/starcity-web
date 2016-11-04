(ns admin.account.entry.views
  (:require [admin.account.entry.security-deposit.views :as security-deposit]
            [re-frame.core :refer [dispatch subscribe]]
            [starcity.components.loading :as loading]
            [starcity.components.icons :as icon]
            [clojure.string :refer [replace capitalize]]))

(defn- overview-bar []
  (let [full-name    (subscribe [:account.entry/full-name])
        role         (subscribe [:account.entry/role])
        phone-number (subscribe [:account.entry/phone-number])
        email        (subscribe [:account.entry/email])]
    (fn []
      [:nav.level.is-mobile {:style {:margin-top "24px"}}
       [:div.level-item
        [:h1.title.is-1 @full-name]]
       [:div.level-item.has-text-centered
        (icon/user)
        [:p.subtitle @role]]
       [:div.level-item.has-text-centered
        (icon/phone)
        [:p.subtitle @phone-number]]
       [:div.level-item.has-text-centered
        (icon/email)
        [:p.subtitle [:a {:href (str "mailto:" @email)} @email]]]])))

(defn- tab-title [tab-key]
  (-> tab-key name (replace #"-" " ") capitalize))

(defn menu [active-item]
  (let [items (subscribe [:account.entry.menu/items])]
    (fn [active-item]
      [:aside.menu
       (doall
        (for [[label tabs] @items]
          ^{:key (str "label-" label)}
          [:div.menu-section
           [:p.menu-label label]
           [:ul.menu-list
            (doall
             (for [tab tabs]
               ^{:key (str "tab-" tab)}
               [:li [:a {:on-click #(dispatch [:account.entry.menu/change-tab tab])
                         :class (when (= tab active-item) "is-active")}
                     (tab-title tab)]]))]]))])))

(defn account []
  (let [is-loading  (subscribe [:account.entry/loading?])
        active-item (subscribe [:account.entry.menu/active])]
    (fn []
      (if @is-loading
        (loading/fill-container "fetching account data...")
        [:div.container
         [overview-bar]
         [:hr]
         [:div.columns
          [:div.column.is-one-quarter
           [menu @active-item]]
          [:div.column
           (case @active-item
             :security-deposit [security-deposit/view]
             [:p [:b (str "No view for " @active-item " yet")]])]]]))))
