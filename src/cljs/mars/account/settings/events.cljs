(ns mars.account.settings.events
  (:require [day8.re-frame.http-fx]
            [mars.account.settings.db :as db]
            [re-frame.core :refer [reg-event-db
                                   reg-event-fx
                                   path]]
            [ajax.core :as ajax]
            [toolbelt.core :as tb]))

(reg-event-db
 :account.settings.change-password/update
 [(path db/path)]
 (fn [db [_ k v]]
   (assoc-in db [:form-data k] v)))

(reg-event-fx
 :account.settings/change-password!
 [(path db/path)]
 (fn [{:keys [db]} [_ {:keys [old-password password-1 password-2]}]]
   {:db         (assoc db :changing-password true)
    :http-xhrio {:method          :post
                 :uri             "/api/v1/mars/settings/change-password"
                 :params          {:old-password old-password
                                   :password-1   password-1
                                   :password-2   password-2}
                 :format          (ajax/transit-request-format)
                 :response-format (ajax/transit-response-format)
                 :on-success      [:account.settings.change-password/success]
                 :on-failure      [:account.settings.change-password/failure]}}))

(reg-event-fx
 :account.settings.change-password/success
 [(path db/path)]
 (fn [{:keys [db]} _]
   {:dispatch      [::done-changing-password]
    :alert/message {:type    :success
                    :content "Password changed."}}))

(reg-event-fx
 :account.settings.change-password/failure
 [(path db/path)]
 (fn [{:keys [db]} [_ {res :response :as error}]]
   (tb/error error)
   {:dispatch     [::done-changing-password]
    :alert/notify {:type    :error
                   :title   "Failed to update password!"
                   :content (or (:message res) "An unknown error occurred.")}}))

(reg-event-db
 ::done-changing-password
 [(path db/path)]
 (fn [db _]
   (-> (assoc db :form-data {})
       (assoc :changing-password false))))
