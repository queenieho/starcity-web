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
  [:aside.menu
   [:p.menu-label "Options"]
   [:ul.menu-list
    (menu-item req "/settings/change-password" "Change Password")
    (menu-item req "/logout" "Log Out")]])

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

;; TODO: condense p/errors into p/messages?
;; or add additional component?
(def account-settings
  (p/page
   (p/title "Account Settings")
   (p/content
    p/navbar
    wrapper)))

(comment
  (def ^:private navbar
    [:nav
     [:div.nav-wrapper.bone-pink.darken-2
      [:h4.nav-title.truncate "Account Settings"]]])

  (defn- wrap-section-content
    [action & children]
    [:div.row
     [:div.col.s10.offset-s1
      [:form {:method "POST" :action action}
       children]]])

  (def ^:private change-password-section
    [:div.section
     (wrap-section-content
      "/account/password"
      [:h5.section-title "Change Password"]
      [:div.section-content
       [:div.row
        [:div.col.s12.m12.l4.input-field
         [:input#current-password {:name "current-password" :type "password" :required true}]
         [:label {:for "current-password"} "Current Password"]]
        [:div.col.s12.m6.l4.input-field
         [:input#password-1 {:name "password-1" :type "password" :required true}]
         [:label {:for "password-1"} "New Password"]]
        [:div.col.s12.m6.l4.input-field
         [:input#password-2 {:name "password-2" :type "password" :required true}]
         [:label {:for "password-2"} "Repeat New Password"]]]
       [:div.row
        [:div.col.s12
         [:button.btn.waves-effect.waves-light.star-green.lighten-1
          {:type "submit"}
          "Update"]]]])])

  (def card-content
    [:div
     change-password-section
     ])

  (defn- content
    [email errors messages]
    [:main#central-card
     [:div.container
      [:div.row {:style "margin-bottom: 0;"}
       (for [error errors]
         [:div.alert.alert-error.col.s12.l10.offset-l1
          [:div.alert-text error]])
       (for [msg messages]
         [:div.alert.alert-success.col.s12.l10.offset-l1
          [:div.alert-text msg]])]
      [:div.row.section
       [:div.col.s12.m12.l10.offset-l1.card-panel.grey-text.text-darken-2
        navbar
        card-content
        [:div.row
         [:div.col.s12.center
          [:a.btn.waves-effect.waves-light.red {:href "/logout"} "Log Out"]]]]]]]))
