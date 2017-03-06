(ns mars.account.rent.events
  (:require [re-frame.core :refer [reg-event-db
                                   reg-event-fx
                                   path]]
            [mars.account.rent.db :as db]
            [mars.account.rent.link-account.events]
            [mars.account.rent.history.events]
            [mars.api :as api]
            [day8.re-frame.http-fx]
            [starcity.log :as l]
            [ajax.core :as ajax]))

;; =============================================================================
;; Bootstrap

(reg-event-fx
 :rent/bootstrap
 (fn [_ _]
   {:dispatch-n [[:rent.upcoming/bootstrap]
                 [:rent.bank-account/bootstrap]
                 [:rent.history/bootstrap]
                 [:rent.security-deposit/bootstrap]]}))

;; =============================================================================
;; Bootstrap Upcoming Payment

(reg-event-fx
 :rent.upcoming/bootstrap
 [(path db/path)]
 (fn [_ _]
   {:dispatch [:rent.upcoming/fetch]}))

(reg-event-fx
 :rent.upcoming/fetch
 [(path db/path)]
 (fn [{:keys [db]} _]
   {:db         (db/set-upcoming-loading db true)
    :http-xhrio {:method          :get
                 :uri             (api/route "/rent/payments/next")
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:rent.upcoming.fetch/success]
                 :on-failure      [:rent.upcoming.fetch/failure]}}))

(reg-event-db
 :rent.upcoming.fetch/failure
 [(path db/path)]
 (fn [db [_ error]]
   (l/error error)
   (db/set-upcoming-loading db false)))

(reg-event-db
 :rent.upcoming.fetch/success
 [(path db/path)]
 (fn [db [_ {:keys [amount due-by] :as res}]]
   (-> (db/set-upcoming-payment db amount due-by)
       (db/set-upcoming-loading false))))

;; =============================================================================
;; Bank Accounts

(reg-event-fx
 :rent.bank-account/bootstrap
 [(path db/path)]
 (fn [_ _]
   {:dispatch [:rent.bank-account/fetch]}))

(reg-event-fx
 :rent.bank-account/fetch
 [(path db/path)]
 (fn [{:keys [db]} _]
   {:db         (db/set-bank-account-loading db true)
    :http-xhrio {:method          :get
                 :uri             (api/route "/rent/bank-account")
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:rent.bank-account.fetch/success]
                 :on-failure      [:rent.bank-account.fetch/failure]}}))

(reg-event-db
 :rent.bank-account.fetch/failure
 [(path db/path)]
 (fn [db [_ error]]
   (l/error error)
   (db/set-bank-account-loading db false)))

(reg-event-fx
 :rent.bank-account.fetch/success
 [(path db/path)]
 (fn [{:keys [db]} [_ {bank-account :bank-account}]]
   (let [fx {:db (-> (db/set-bank-account db bank-account)
                     (db/set-bank-account-loading false))}]
     ;; If there is a linked bank account, fetch the autopay status
     (if bank-account
       (assoc fx :dispatch [:rent.autopay/fetch-status])
       fx))))

;; =============================================================================
;; Link Bank Account

(reg-event-fx
 :rent/toggle-link-account
 [(path db/path)]
 (fn [{:keys [db]} _]
   (when-not (db/bank-account-linked? db)
     {:db       (db/toggle-link-account db)
      :dispatch [:rent.link-account/bootstrap]})))

(reg-event-fx
 :rent.link-account/complete
 [(path db/path)]
 (fn [{:keys [db]} _]
   {:db           (db/toggle-link-account db)
    :dispatch     [:rent.bank-account/fetch]
    :alert/notify {:title    "Bank Account Linked!"
                   :content  "You can now make rent payments with your bank account and subscribe to autopay!"
                   :duration 8
                   :type     :success}}))

;; =============================================================================
;; Enable Autopay

(reg-event-fx
 :rent/show-autopay
 [(path db/path)]
 (fn [{:keys [db]} _]
   (let [dispatch (if (db/bank-account-linked? db)
                    [:rent/toggle-show-autopay]
                    [:rent/toggle-link-account])]
     {:dispatch-later [{:ms 1500 :dispatch dispatch}]
      :alert/message  {:type     :loading
                       :content  "Loading..."
                       :duration 1.5}})))

(reg-event-db
 :rent/toggle-show-autopay
 [(path db/path)]
 (fn [db _]
   (if-not (db/autopay-enabled? db)
     (db/toggle-show-autopay db)
     db)))

(reg-event-fx
 :rent.autopay/fetch-status
 [(path db/path)]
 (fn [{:keys [db]} _]
   {:db         (db/toggle-fetching-autopay-status db)
    :http-xhrio {:method          :get
                 :uri             (api/route "/rent/autopay/subscribed")
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:rent.autopay.fetch-status/success]
                 :on-failure      [:rent.autopay.fetch-status/failure]}}))

(reg-event-db
 :rent.autopay.fetch-status/success
 [(path db/path)]
 (fn [db [_ {subscribed :subscribed}]]
   (-> (db/set-autopay-enabled db subscribed)
       (db/toggle-fetching-autopay-status))))

(reg-event-db
 :rent.autopay.fetch-status/failure
 [(path db/path)]
 (fn [db [_ res]]
   ;; TODO: What to do here?
   (l/error res)
   (db/toggle-fetching-autopay-status db)))

(reg-event-fx
 :rent.autopay/enable
 [(path db/path)]
 (fn [{:keys [db]} _]
   {:db            (db/toggle-enabling-autopay db)
    :alert/message {:type     :loading
                    :duration :indefinite
                    :content  "Loading..."}
    :http-xhrio    {:method          :post
                    :uri             (api/route "/rent/autopay/subscribe")
                    :format          (ajax/json-request-format)
                    :response-format (ajax/json-response-format {:keywords? true})
                    :on-success      [:rent.autopay.enable/success]
                    :on-failure      [:rent.autopay.enable/failure]}}))

(reg-event-fx
 :rent.autopay.enable/success
 [(path db/path)]
 (fn [{:keys [db]} _]
   {:db            (-> (db/toggle-enabling-autopay db)
                       (db/toggle-show-autopay))
    :dispatch      [:rent.autopay/fetch-status]
    :alert/message {:type    :success
                    :content "Autopay enabled!"}}))

(reg-event-fx
 :rent.autopay.enable/failure
 [(path db/path)]
 (fn [{:keys [db]} _]
   {:db                 (db/toggle-enabling-autopay db)
    :alert.message/hide true
    :alert/notify       {:type    :error
                         :title   "Failed to enable autopay."
                         :content "Please check your internet connection and try again."}}))

;; =============================================================================
;; Make Payment

(reg-event-fx
 :rent/make-payment
 [(path db/path)]
 (fn [{:keys [db]} [_ payment]]
   {:db       (db/set-payment db payment)
    :dispatch [:rent.make-payment/toggle-show]}))

(reg-event-db
 :rent.make-payment/toggle-show
 [(path db/path)]
 (fn [db _]
   (db/toggle-show-make-payment db)))

(reg-event-fx
 :rent.make-payment/pay
 [(path db/path)]
 (fn [{:keys [db]} [_ payment-id]]
   {:db            (db/toggle-paying db)
    :alert/message {:type     :loading
                    :content  "Processing..."
                    :duration :indefinite}
    :http-xhrio    {:method          :post
                    :uri             (api/route (str "/rent/payments/" payment-id "/pay"))
                    :format          (ajax/json-request-format)
                    :response-format (ajax/json-response-format {:keywords? true})
                    :on-success      [:rent.make-payment.pay/success payment-id]
                    :on-failure      [:rent.make-payment.pay/failure]}}))

(def ^:private payment-failure-msg
  "We failed to process your payment. Please try again or contact us for details.")

(reg-event-fx
 :rent.make-payment.pay/failure
 [(path db/path)]
 (fn [{:keys [db]} [_ error]]
   (l/error error)
   {:db                 (db/toggle-paying db)
    :dispatch           [:rent.make-payment/toggle-show]
    :alert.message/hide true
    :alert/notify       {:type     :error
                         :duration 6.0
                         :title    "Payment Failed"
                         :content  payment-failure-msg}}))

(def ^:private payment-success-msg
  "Your payment has been successfully processed! Please allow 1-3 business days
  for the transaction to complete.")

(reg-event-fx
 :rent.make-payment.pay/success
 [(path db/path)]
 (fn [{:keys [db]} [_ payment-id res]]
   {:db                 (db/toggle-paying db)
    :dispatch-n         [[:rent.make-payment/toggle-show]
                         [:rent.history/set-pending-ach payment-id]]
    :alert.message/hide true
    :alert/notify       {:type     :success
                         :duration 6.0
                         :title    "Payment Succeeded"
                         :content  payment-success-msg}}))

;; =============================================================================
;; Security Deposit

(reg-event-fx
 :rent.security-deposit/bootstrap
 [(path db/path)]
 (fn [_ _]
   {:dispatch [:rent.security-deposit/fetch]}))

(reg-event-fx
 :rent.security-deposit/fetch
 [(path db/path)]
 (fn [{:keys [db]} _]
   {:db         (db/fetching-security-deposit db)
    :http-xhrio {:method          :get
                 :uri             (api/route "/security-deposit")
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:rent.security-deposit.fetch/success]
                 :on-failure      [:rent.security-deposit.fetch/failure]}}))

(reg-event-db
 :rent.security-deposit.fetch/success
 [(path db/path)]
 (fn [db [_ result]]
   (db/set-security-deposit db (:result result))))

(reg-event-db
 :rent.security-deposit.fetch/failure
 [(path db/path)]
 (fn [db [_ err]]
   (l/error "security deposit:" err)
   (db/set-security-deposit-error db err)))

;; =====================================
;; Confirmation

(reg-event-db
 :rent.security-deposit/show-confirmation
 [(path db/path)]
 (fn [db _]
   (db/show-security-deposit-confirmation db)))

(reg-event-db
 :rent.security-deposit/hide-confirmation
 [(path db/path)]
 (fn [db _]
   (if-not (db/paying-security-deposit? db)
     (db/hide-security-deposit-confirmation db)
     db)))

;; =====================================
;; Payment

(reg-event-fx
 :rent.security-deposit/pay
 [(path db/path)]
 (fn [{:keys [db]} _]
   {:db            (db/paying-security-deposit db)
    :alert/message {:type     :loading
                    :duration :indefinite
                    :content  "Submitting payment..."}
    :http-xhrio    {:method          :post
                    :uri             (api/route "/security-deposit/pay")
                    :response-format (ajax/json-response-format {:keywords? true})
                    :format          (ajax/json-request-format)
                    :on-success      [:rent.security-deposit.pay/success]
                    :on-failure      [:rent.security-deposit.pay/failure]}}))

(def ^:private deposit-success-msg
  "Your payment has been successfully submitted! Thanks!")

(reg-event-fx
 :rent.security-deposit.pay/success
 [(path db/path)]
 (fn [{:keys [db]} _]
   {:db                 (db/set-paying-security-deposit db false)
    :alert.message/hide true
    :alert/notify       {:type     :success
                         :duration 8.0
                         :title    "Payment Submitted"
                         :content  deposit-success-msg}
    :dispatch-n         [[:rent.security-deposit/fetch]
                         [:rent.security-deposit/hide-confirmation]]}))

(def ^:private deposit-failure-msg
  "We failed to process your payment. Please try again, and be sure to check your network connection.")

(reg-event-fx
 :rent.security-deposit.pay/failure
 [(path db/path)]
 (fn [{:keys [db]} _]
   {:db                 (db/set-paying-security-deposit db false)
    :dispatch           [:rent.security-deposit/hide-confirmation]
    :alert.message/hide true
    :alert/notify       {:type     :error
                         :duration 8.0
                         :title    "Payment Failed"
                         :content  deposit-failure-msg}}))
