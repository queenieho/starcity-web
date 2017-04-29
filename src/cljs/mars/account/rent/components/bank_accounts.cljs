(ns mars.account.rent.components.bank-accounts
  (:require [mars.components.antd :as a]
            [starcity.components.icons :as i]))

(defn- bank-name [name]
  [:span.tag.is-info.is-small name])

(defn- bank-number [number]
  [:p
   [:span {:style {:margin-right 4 :margin-left 16}}
    (for [i (range 4)]
      ^{:key (str "dot-" i)} [:span {:dangerouslySetInnerHTML {:__html "&middot;"}}])]
   number])

(defn- payment-source [bank-account]
  [:div.columns.is-mobile
   [:div.column.has-text-centered
    [:p.heading "Bank Name"]
    (bank-name (:bank-name bank-account))]
   [:div.column.has-text-centered
    [:p.heading "Account"]
    (bank-number (:number bank-account))]])

(defn- link-account [on-link-account]
  [:div
   [:p "Adding a bank account is the fastest and easiest way to pay your rent."]
   [:div.controls {:style {:text-align "center"
                           :margin-top "16px"}}
    [a/button {:type "primary" :on-click on-link-account}
     "Link Bank Account"]]])

(defn bank-accounts
  [bank-account loading autopay
   & {:keys [on-link-account on-enable-autopay]
      :or   {on-link-account   identity
             on-enable-autopay identity}}]
  [a/card {:title   "Bank Accounts"
           :loading loading}
   (if bank-account
     [payment-source bank-account autopay]
     [link-account on-link-account])

   (when bank-account
     [:div {:style {:text-align "center"}}
      [a/button {:type     "primary"
                 :loading  (:fetching autopay)
                 :disabled (:enabled autopay)
                 :on-click on-enable-autopay
                 :icon     (if (:enabled autopay) "check" "info-circle")}
       (if (:enabled autopay)
         "Autopay Enabled"
         "Enable Autopay")]])])
