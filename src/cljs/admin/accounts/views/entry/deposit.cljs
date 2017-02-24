(ns admin.accounts.views.entry.deposit
  (:require [admin.accounts.check-form.views :as check-form]
            [admin.components.util :as u]
            [ant-ui.core :as a]
            [clojure.string :as string]
            [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as r]
            [toolbelt.core :as tb]
            [toolbelt.date :as date]))

(defn- edit-check [data]
  (let [check (update data :status #(keyword "check.status" %))]
    #(dispatch [:check-form/show check])))

(def ^:private columns
  [{:title     "Amount"
    :dataIndex "amount"
    :key       "amount"
    :render    #(str "$" %)}
   {:title     "Method"
    :dataIndex "method"
    :key       "method"
    :render    #(-> (or % "N/A") name string/upper-case)}
   {:title     "Status"
    :dataIndex "status"
    :key       "status"}
   {:title     "Actions"
    :dataIndex "actions"
    :render    (fn [_ record]
                 (let [record (js->clj record :keywordize-keys true)]
                   (when (= (:method record) "check")
                     (r/as-element
                      [:a {:on-click (edit-check (:payment record))} "Edit"]))))}])

(defmulti deposit-content :method)

(defmethod deposit-content "check"
  [{payment :payment}]
  [:p
   [:b "check number: "] (:number payment) u/divider
   [:b "bank: "] (:bank payment) u/divider
   [:b "date on check: "] (date/short-date (:date payment)) u/divider
   [:b "received on: "] (date/short-date (:received-on payment))])

(defmethod deposit-content "ach"
  [{payment :payment}]
  [:p "View payment details on the "
   [:a {:href   (:stripe-uri payment)
        :target "_blank"}
    "Stripe dashboard."]])

(defn- payment->row [payment]
  {:key     (:payment/id payment)
   :amount  (:payment/amount payment)
   :status  (:payment/status payment)
   :method  (:payment/method payment)
   :payment payment})

(defn add-check-button [deposit-id]
  (let [name (subscribe [:account/name])]
    (fn [deposit-id]
      [:a {:on-click #(dispatch [:check-form/show {:deposit-id deposit-id
                                                   :name       @name
                                                   :amount     500}])}
       "Add Check"])))

(defn payments
  "Component that displays the current account's security deposit payments in
  tabular format."
  []
  (let [deposit (subscribe [:account/deposit])]
    (fn []
      (let [payments (:deposit/payments @deposit)]
        [:div
         [check-form/modal]
         [a/card {:title "Security Deposit Payments"
                  :extra (r/as-element [add-check-button (:db/id @deposit)])}
          (if (empty? payments)
            [:p "No payments yet."]
            [a/table {:dataSource        (clj->js (map payment->row payments))
                      :columns           columns
                      :expandedRowRender (fn [payment]
                                           (r/as-element (deposit-content (js->clj payment :keywordize-keys true))))
                      :size              :small}])]]))))
