(ns starcity.views.account
  (:require [starcity.views.base :refer [base]]
            [starcity.models.account :as account]))

;; =============================================================================
;; Helpers
;; =============================================================================

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

(def ^:private change-email-section
  [:div.section
   (wrap-section-content
    "/api/v1/account/email"
    [:h5.section-title "Change Email"]
    [:div.section-content
     [:div.row
      [:div.col.s12.m6.input-field
       [:input#email {:name "email" :type "email" :required true}]
       [:label {:for "email"} "New Email Address"]]
      [:div.col.s12.m6.input-field
       [:input#password {:name "password" :type "password" :required true}]
       [:label {:for "password"} "Your Password"]]]
     [:div.row
      [:div.col.s12
       [:button.btn.waves-effect.waves-light.star-green.lighten-1
        {:type "submit"}
        "Update"]]]])])

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

(defn- card-content
  [account]
  [:div
   change-password-section
   ;; change-email-section ;; TODO:
   ])

(defn- content
  [email errors messages]
  (let [acct (account/by-email email)]
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
        (card-content acct)
        [:div.row
         [:div.col.s12.center
          [:a.btn.waves-effect.waves-light.red {:href "/logout"} "Log Out"]]]]]]]))

;; =============================================================================
;; API
;; =============================================================================

(defn account-settings
  [{:keys [identity] :as req} & {:keys [errors messages]
                                 :or   {errors [], messages []}}]
  (base
   :req req
   :title "Account Settings"
   :content (content (:account/email identity) errors messages)))
