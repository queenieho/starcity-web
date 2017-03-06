(ns mars.account.settings.views
  (:require [mars.components.pane :as pane]
            [re-frame.core :refer [dispatch subscribe]]
            [ant-ui.core :as a]
            [reagent.core :as r]
            [clojure.string :as string]))

(defn- on-change [data k]
  (fn [e]
    (dispatch [:account.settings.change-password/update k (.. e -target -value)])))

(defn- attrs [data k]
  {:type      "password"
   :value     (get @data k)
   :on-change (on-change data k)})

(defn- valid?
  [{:keys [old-password password-1 password-2]}]
  (not
   (or (string/blank? old-password)
       (string/blank? password-1)
       (string/blank? password-2))))

(defn- change-password []
  (let [form-data   (subscribe [:account.settings.change-password/form-data])
        is-changing (subscribe [:account.settings.change-password/changing?])]
    [a/card {:title "Change Your Password"}
     [:form
      {:on-submit #(do
                     (.preventDefault %)
                     (dispatch [:account.settings/change-password! @form-data]))}
      [:div.control.is-grouped

       [:div.control.is-expanded
        [:label.label "Old Password"]
        [a/input (attrs form-data :old-password)]]

       [:div.control.is-expanded
        [:label.label "New Password"]
        [a/input (attrs form-data :password-1)]]

       [:div.control.is-expanded
        [:label.label "Repeat New Password"]
        [a/input (attrs form-data :password-2)]]]

      [:div.pull-right
       {:style {:padding-bottom 10}}
       [a/button
        {:type      :primary
         :size      :large
         :html-type :submit
         :loading   @is-changing
         :disabled  (not (valid? @form-data))}
        "Change Password"]]]]))

(defn view []
  [:div
   (pane/pane
    (pane/header "Account Settings")
    (pane/content [change-password]))])
