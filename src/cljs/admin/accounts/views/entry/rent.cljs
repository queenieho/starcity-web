(ns admin.accounts.views.entry.rent
  (:require [admin.components.util :as u]
            [ant-ui.core :as a]
            [clojure.string :refer [upper-case]]
            [re-frame.core :refer [dispatch subscribe]]
            [toolbelt.date :as date]
            [toolbelt.core :as tb]
            [reagent.core :as r]))

(defn- add-check [name {:keys [amount key]}]
  #(dispatch [:check-form/show {:payment-id key
                                :name       name
                                :amount     amount}]))
(defn- edit-check [data]
  (let [check (update data :status #(keyword "check.status" %))]
    #(dispatch [:check-form/show check])))

(defn- columns [name]
  [{:title     "Amount"
    :dataIndex "amount"
    :key       "amount"
    :render    #(str "$" %)}
   {:title     "Period"
    :dataIndex "period"
    :key       "period"
    :render    (fn [[start end]]
                 (str (date/short-date start) "-" (date/short-date end)))}
   {:title     "Status"
    :dataIndex "status"
    :key       "status"}
   {:title     "Paid On"
    :dataIndex "paid-on"
    :key       "paid-on"
    :render    #(if % (date/short-date %) "N/A")}
   {:title     "Due Date"
    :dataIndex "due-date"
    :key       "due-date"
    :render    #(date/short-date %)}
   {:title     "Method"
    :dataIndex "method"
    :key       "method"
    :render    #(-> (or % "N/A") upper-case)}
   {:title     "Actions"
    :dataIndex "actions"
    :render    (fn [_ record]
                 (let [record (js->clj record :keywordize-keys true)]
                   (r/as-element
                    (cond
                      (= (:method record) "check")
                      [:a {:on-click (edit-check (:check record))} "Edit"]

                      (nil? (:method record))
                      [:a {:on-click (add-check name record)} "Add Check"]

                      :otherwise [:span]))))}])

(defn- payment->row [payment]
  {:key        (:db/id payment)
   :amount     (:payment/amount payment)
   :status     (name (:payment/status payment))
   :due-date   (:payment/due payment)
   :paid-on    (:payment/paid-on payment)
   :period     [(:payment/pstart payment)
                (:payment/pend payment)]
   :method     (when-let [method (:payment/method payment)] (name method))
   :check      (:payment/check payment)
   :stripe-uri (:payment/stripe-uri payment)})

;; NOTE: Lots of duplication between this and `deposit.cljs`
(defmulti payment-content :method)

(defmethod payment-content :default [_]
  [:p "No details."])

(defmethod payment-content "check" [{check :check}]
  [:p
   [:b "check number: "] (:number check) u/divider
   [:b "bank: "] (:bank check) u/divider
   [:b "date on check: "] (date/short-date (:date check)) u/divider
   [:b "received on: "] (date/short-date (:received-on check))])

(defn- stripe-uri [payment]
  [:p "View payment details on the "
   [:a {:href   (:stripe-uri payment)
        :target "_blank"}
    "Stripe dashboard."]])

(defmethod payment-content "ach" [payment]
  (stripe-uri payment))

(defmethod payment-content "autopay" [payment]
  (stripe-uri payment))

(defn- expanded-row [payment]
  (r/as-element [payment-content payment]))

(defn payments
  "Component that displays the current account's rent payments in tabular format."
  []
  (let [name     (subscribe [:account/name])
        payments (subscribe [:account/rent-payments])]
    (fn []
      [a/table {:dataSource        (clj->js (map payment->row @payments))
                :columns           (columns @name)
                :expandedRowRender (comp expanded-row #(js->clj % :keywordize-keys true))
                :size              :small}])))
