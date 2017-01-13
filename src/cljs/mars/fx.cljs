(ns mars.fx
  (:require [re-frame.core :refer [reg-fx dispatch]]
            [mars.fx.message]
            [mars.fx.notification]
            [starcity.log :as l]))

(reg-fx
 :stripe.bank-account/create-token
 (fn [{:keys [key on-success on-failure] :as args}]
   (.setPublishableKey js/Stripe key)
   (.createToken js/Stripe.bankAccount
                 #js {:country             (:country args)
                      :currency            (:currency args)
                      :routing_number      (:routing-number args)
                      :account_number      (:account-number args)
                      :account_holder_name (:account-holder-name args)
                      :account_holder_type (:account-holder-type args)}
                 (fn [status response]
                   (let [response (js->clj response :keywordize-keys true)]
                     (if-let [e (:error response)]
                      (dispatch (conj on-failure e))
                      (dispatch (conj on-success response))))))))

(reg-fx
 :plaid/auth
 (fn [{:keys [key env on-success on-failure on-exit]}]
   (let [handler ((aget js/Plaid "create") #js
                  {:env           env
                   :clientName    "Starcity"
                   :key           key
                   :product       "auth"
                   :selectAccount true
                   :onSuccess     (fn [public-token meta]
                                    (dispatch (-> (conj on-success public-token)
                                                  (conj (js->clj meta :keywordize-keys true)))))
                   :onExit        (fn [error metadata]
                                    (if-let [e error]
                                      (dispatch (conj on-failure (js->clj error :keywordize-keys true)))
                                      (dispatch (conj on-exit (js->clj metadata :keywordize-keys true)))))})]
     (.open handler))))
