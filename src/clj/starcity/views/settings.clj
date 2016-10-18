(ns starcity.views.settings
  (:require [starcity.views.page :as p]
            [starcity.views.components.layout :as l]
            [starcity.views.components.form :as f]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn menu-item
  [req href label]
  (let [active (= href (get req :uri))]
    [:li
     [:a {:href href :class (when active "is-active")}
      label]]))

(defn- menu [req]
  (let [-menu-item (partial menu-item req)]
    [:aside.menu
     [:p.menu-label "Options"]
     [:ul.menu-list
      (-menu-item "/settings/change-password" "Change Password")
      (-menu-item "/logout" "Log Out")]]))

(defmulti content :path-info)

(defmethod content "/change-password" [_]
  [:div
   [:p.title.is-4 "Change your password"]
   [:div.content
    [:p "To change your password:"]
    [:ol
     [:li "Enter your <strong>current password</strong> to verify that you're actually the account owner, and then"]
     [:li "Enter your desired new password <strong>twice</strong> to prevent mistakes."]]]
   (l/box
    [:form {:action "/settings/change-password" :method "POST"}
     [:div.control.columns
      [:div.control.column
       (f/label "current-password" "Current Password")
       [:input.input
        {:name "current-password" :type "password" :required true}]]
      [:div.control.column
       (f/label "password-1 ""New Password")
       [:input.input
        {:name "password-1" :type "password" :required true}]]
      [:div.control.column
       (f/label "password-2" "Repeat New Password")
       [:input.input
        {:name "password-2" :type "password" :required true}]]]
     (f/control
      [:button.button.is-info {:type "submit"} "Change Password"])])])

(defmethod content :default [_]
  [:div]
  [:p.title.is-4 "ERROR: Setting not found!"])

(defn- wrapper [req]
  (l/section
   {:style "flex-grow: 1"}
   (l/container
    (l/columns
     (l/column
      {:class "is-one-quarter"}
      (menu req))
     (l/column
      (p/messages req)
      (content req))))))

;; =============================================================================
;; API
;; =============================================================================

(def account-settings
  (p/page
   (p/title "Account Settings")
   p/navbar
   wrapper))
