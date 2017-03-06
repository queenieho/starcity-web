(ns mars.account.rent.link-account.authorize.events
  (:require [re-frame.core :refer [reg-event-db
                                   reg-event-fx
                                   path]]
            [mars.account.rent.link-account.authorize.db :as db]
            [mars.api :as api]
            [day8.re-frame.http-fx]
            [starcity.log :as l]
            [ajax.core :as ajax]))

(reg-event-db
 :rent.link-account.authorize/bootstrap
 [(path db/path)]
 (fn [db [_ data]]
   (let [plan (get-in data [:setup :plan])]
     (db/set-plan db plan))))

(reg-event-db
 :rent.link-account.authorize.acknowledge/toggle
 [(path db/path)]
 (fn [db]
   (db/toggle-authorized db)))

(reg-event-fx
 :rent.link-account.authorize/submit
 [(path db/path)]
 (fn [{:keys [db]} _]
   (let [authorized (db/authorized? db)]
     {:db            (db/toggle-subscribing db)
      :alert/message {:type     :loading
                      :duration :indefinite
                      :content  "Hold tight, this may take a moment..."}
      :http-xhrio    {:method          :post
                      :uri             (api/route "/rent/autopay/subscribe")
                      :format          (ajax/json-request-format)
                      :response-format (ajax/json-response-format {:keywords? true})
                      :on-success      [:rent.link-account.authorize.submit/success]
                      :on-failure      [:rent.link-account.authorize.submit/failure]}})))

(reg-event-fx
 :rent.link-account.authorize.submit/success
 [(path db/path)]
 (fn [{:keys [db]} [_ response]]
   {:db                 (db/toggle-subscribing db)
    :alert.message/hide true
    :dispatch-n         [[:rent.autopay/fetch-status]
                         [:rent.upcoming/fetch]
                         [:rent/hide-autopay]]
    :alert/notify       {:type    :success
                         :title   "Success!"
                         :content "You have been subscribed to autopay!"}}))

(def ^:private failure-notification
  {:type     :error
   :duration 6.0
   :title    "Something went wrong..."
   :content  "Either you're disconnected from the internet or there's a bug on our end."})

(reg-event-fx
 :rent.link-account.authorize.submit/failure
 [(path db/path)]
 (fn [{:keys [db]} [_ error]]
   (l/error error)
   {:db                 (db/toggle-subscribing db)
    :alert.message/hide true
    :alert/notify       failure-notification}))
