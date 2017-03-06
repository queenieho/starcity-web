(ns mars.account.rent.link-account.events
  (:require [re-frame.core :refer [reg-event-db
                                   reg-event-fx
                                   path]]
            [mars.account.rent.link-account.db :as db]
            [mars.account.rent.link-account.deposits.events]
            [mars.account.rent.link-account.authorize.events]
            [mars.api :as api]
            [day8.re-frame.http-fx]
            [starcity.log :as l]
            [ajax.core :as ajax]))

(reg-event-fx
 :rent.link-account/bootstrap
 [(path db/path)]
 (fn [{:keys [db]} _]
   {:db         (db/toggle-loading db)
    :http-xhrio {:method          :get
                 :uri             (api/route "/rent/bank-account/setup")
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:rent.link-account.bootstrap/success]
                 :on-failure      [:rent.link-account.bootstrap/failure]}}))

(reg-event-fx
 :rent.link-account.bootstrap/success
 [(path db/path)]
 (fn [{:keys [db]} [_ data]]
   (let [{:keys [plaid setup]} data]
     {:db         (-> (db/set-plaid-key db (:public-key plaid))
                      (db/set-plaid-env (:env plaid))
                      (db/toggle-loading))
      :dispatch-n [[:rent.link-account.deposits/bootstrap data]
                   ;; TODO: remove
                   [:rent.link-account.authorize/bootstrap data]
                   [:rent.link-account.status/update (:status setup)]]})))

(reg-event-fx
 :rent.link-account.bootstrap/failure
 [(path db/path)]
 (fn [{:keys [db]} [_ error]]
   (l/error error)
   {:db (db/toggle-loading db)}))

(reg-event-db
 :rent.link-account.status/update
 [(path db/path)]
 (fn [db [_ status]]
   (db/set-status db status)))

;; =============================================================================
;; UI

(reg-event-db
 :rent.link-account/deposits
 [(path db/path)]
 (fn [db _]
   (db/set-status db :bank-needed)))

(reg-event-db
 :rent.link-account/choose-method
 [(path db/path)]
 (fn [db _]
   (db/set-status db :none)))

;; =============================================================================
;; Plaid

(reg-event-fx
 :rent.link-account/plaid
 [(path db/path)]
 (fn [{:keys [db]} _]
   {:db         (db/toggle-plaid-loading db)
    :plaid/auth {:key        (db/plaid-key db)
                 :env        (db/plaid-env db)
                 :on-success [:rent.link-account.plaid/success]
                 :on-failure [:rent.link-account.plaid/failure]
                 :on-exit    [:rent.link-account.plaid/exit]}}))

(reg-event-fx
 :rent.link-account.plaid/success
 [(path db/path)]
 (fn [{:keys [db]} [_ public-token meta]]
   {:dispatch [:rent.link-account.plaid/verify public-token (:account_id meta)]}))

(def ^:private plaid-failure-msg
  "Something unexpected happened while submitting your credentials. Please try again and be sure to check your internet connection.")

(reg-event-fx
 :rent.link-account.plaid/failure
 [(path db/path)]
 (fn [{:keys [db]} [_ error]]
   {:db           (db/toggle-plaid-loading db)
    :alert/notify {:type    :error
                   :title   "Oops!"
                   :content plaid-failure-msg}}))

(reg-event-db
 :rent.link-account.plaid/exit
 [(path db/path)]
 (fn [db [_ data]]
   (db/toggle-plaid-loading db)))

(reg-event-fx
 :rent.link-account.plaid/verify
 [(path db/path)]
 (fn [{:keys [db]} [_ public-token account-id]]
   {:alert/message {:type     :loading
                    :duration :indefinite
                    :content  "Verifying..."}
    :http-xhrio    {:method          :post
                    :uri             (api/route "/rent/bank-account/setup/plaid/verify")
                    :params          {:account-id   account-id
                                      :public-token public-token}
                    :format          (ajax/json-request-format)
                    :response-format (ajax/json-response-format {:keywords? true})
                    :on-success      [:rent.link-account.plaid.verify/success]
                    :on-failure      [:rent.link-account.plaid.verify/failure]}}))

(reg-event-fx
 :rent.link-account.plaid.verify/success
 [(path db/path)]
 (fn [{:keys [db]} [_ result]]
   {:db                 (db/toggle-plaid-loading db)
    :alert.message/hide true
    :dispatch           [:rent.link-account/complete]}))

(reg-event-fx
 :rent.link-account.plaid.verify/failure
 [(path db/path)]
 (fn [{:keys [db]} [_ error]]
   {:db                 (db/toggle-plaid-loading db)
    :alert.message/hide true
    :alert/notify       {:type    :error
                         :title   "Oops!"
                         :content plaid-failure-msg}}))
