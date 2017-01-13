(ns mars.account.rent.link-account.deposits.events
  (:require [re-frame.core :refer [reg-event-db
                                   reg-event-fx
                                   path]]
            [mars.account.db :as account]
            [mars.account.rent.link-account.deposits.db :as db]
            [mars.api :as api]
            [day8.re-frame.http-fx]
            [starcity.log :as l]
            [ajax.core :as ajax]))

(reg-event-fx
 :rent.link-account.deposits/bootstrap
 [(path db/path)]
 (fn [{:keys [db]} [_ data]]
   (let [{:keys [stripe countries]} data]
     {:db       (-> (db/set-stripe-key db (:public-key stripe))
                    (db/set-countries countries))
      :dispatch [::populate-account-holder]})))

(reg-event-db
 ::populate-account-holder
 (fn [db _]
   (let [full-name (-> db account/path account/full-name)]
     (assoc db db/path (db/set-account-holder (get db db/path) full-name)))))

(defn- validate-bank-info
  [{:keys [country account-number routing-number]}]
  (merge (db/validate-account-number account-number country)
         (db/validate-routing-number routing-number country)))

(reg-event-db
 :rent.link-account.deposits.bank-info/update
 [(path db/path)]
 (fn [db [_ k v]]
   (let [db' (db/update-form-field db k v)]
     (if (db/submitted? db)
       (let [errors (validate-bank-info (db/form-data db'))]
         (db/set-form-errors db' errors))
       db'))))

(reg-event-fx
 :rent.link-account.deposits.bank-info/submit
 [(path db/path)]
 (fn [{:keys [db]} _]
   (let [form-data (db/form-data db)
         errors    (validate-bank-info form-data)]
     (if (empty? errors)
       {:db            (db/toggle-submitting-bank-info db)
        :alert/message {:type     :loading
                        :content  "Submitting bank details..."
                        :duration :indefinite}
        :stripe.bank-account/create-token
        {:country             (:country form-data)
         :currency            (:currency form-data)
         :routing-number      (:routing-number form-data)
         :account-number      (:account-number form-data)
         :account-holder-name (:account-holder form-data)
         ;; NOTE: may need to parameterize this at some point
         :account-holder-type "individual"
         :key                 (db/stripe-key db)
         :on-success          [:autopay.create-token/success]
         :on-failure          [:autopay.create-token/failure]}}
       {:db (-> (db/set-form-errors db errors)
                (db/set-submitted))}))))

(def ^:private default-error
  {:content  "Oops! Something went wrong."
   :duration 3.0
   :type     :error})

(reg-event-fx
 :autopay.create-token/failure
 [(path db/path)]
 (fn [{:keys [db]} [_ error]]
   (l/error "Failed to create token!" error)
   {:db            (db/toggle-submitting-bank-info db)
    :alert/message default-error}))

(reg-event-fx
 :autopay.create-token/success
 [(path db/path)]
 (fn [{:keys [db]} [_ stripe-response]]
   {:db         (db/toggle-submitting-bank-info db)
    :http-xhrio {:method          :post
                 :uri             (api/route "/rent/bank-account/setup/deposits")
                 :params          {:stripe-token (:id stripe-response)}
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:autopay.send-bank-token/success]
                 :on-failure      [:autopay.send-bank-token/failure]}}))

(reg-event-fx
 :autopay.send-bank-token/success
 [(path db/path)]
 (fn [{:keys [db]} [_ response]]
   (l/log response)
   {:dispatch      [:rent.link-account.status/update (:status response)]
    :alert/message {:content "Success!"
                    :type    :success}}))

(reg-event-fx
 :autopay.send-bank-token/failure
 [(path db/path)]
 (fn [_ [_ error]]
   (l/error "Failed to submit token to server!" error)
   {:alert/message default-error}))

;; =============================================================================
;; Deposit Verification

(reg-event-db
 :rent.link-account.deposits.amounts/update
 [(path db/path)]
 (fn [db [_ idx v]]
   (db/set-deposit-amount db idx (js/parseInt v))))

(reg-event-fx
 :rent.link-account.deposits/verify-amounts
 [(path db/path)]
 (fn [{:keys [db]} _]
   {:db            (db/toggle-submitting-deposits db)
    :http-xhrio    {:method          :post
                    :uri             (api/route "/rent/bank-account/setup/deposits/verify")
                    :params          {:deposits (db/deposit-amounts db)}
                    :format          (ajax/json-request-format)
                    :response-format (ajax/json-response-format {:keywords? true})
                    :on-success      [:rent.link-account.deposits.verify-amounts/success]
                    :on-failure      [:rent.link-account.deposits.verify-amounts/failure]}
    :alert/message {:content  "Verifying deposits..."
                    :type     :loading
                    :duration :indefinite}}))

(reg-event-fx
 :rent.link-account.deposits.verify-amounts/success
 [(path db/path)]
 (fn [{:keys [db]} [_ response]]
   {:db                 (db/toggle-submitting-deposits (db/path db))
    :alert.message/hide true
    :dispatch           [:rent.link-account/complete]}))

(reg-event-fx
 :rent.link-account.deposits.verify-amounts/failure
 [(path db/path)]
 (fn [{:keys [db]} _]
   {:db            (db/toggle-submitting-deposits db)
    :alert/message {:type     :error
                    :duration 6
                    :content  "Incorrect deposit amounts entered."}}))
