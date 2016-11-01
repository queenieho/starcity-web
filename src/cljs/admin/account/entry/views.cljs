(ns admin.account.entry.views
  (:require [admin.account.entry.db :refer [root-db-key]]
            [re-frame.core :refer [dispatch subscribe]]
            [starcity.components.loading :as loading]
            [starcity.components.tabs :as t]
            [starcity.components.icons :as icon]))

(defn- overview-bar []
  (let [full-name    (subscribe [:account.entry/full-name])
        role         (subscribe [:account.entry/role])
        phone-number (subscribe [:account.entry/phone-number])
        email        (subscribe [:account.entry/email])]
    (fn []
      [:nav.level {:style {:margin-top "24px"}}
       [:div.level-item.has-text-centered
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

(defn- security-deposit []
  [:div "TODO:"])

(defn account []
  (let [is-loading (subscribe [:account.entry/loading?])
        active-tab (t/subscribe-active-tab root-db-key)]
    (fn []
      (if @is-loading
        (loading/fill-container "fetching account data...")
        [:div.container
         [overview-bar]
         [t/tabs root-db-key]
         (case @active-tab
           :security-deposit [security-deposit]
           [:p [:b "Error!"]])]))))
