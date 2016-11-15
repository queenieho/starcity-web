(ns admin.application.entry.events
  (:require [admin.application.entry.db :refer [root-db-key]]
            [admin.application.entry.model :as model]
            [re-frame.core :refer [reg-event-fx
                                   reg-event-db]]
            [day8.re-frame.http-fx]
            [starcity.log :as l]
            [admin.api :as api]
            [ajax.core :as ajax]))

(reg-event-fx
 :nav/application
 (fn [{:keys [db]} [_ id]]
   {:db       (-> (assoc db :route :application/entry)
                  (model/active-tab :move-in)
                  (model/current-id id))
    :dispatch [:application.entry/fetch id]}))

(reg-event-fx
 :application.entry/fetch
 (fn [{:keys [db]} [_ id]]
   {:db         (model/toggle-loading db)
    :http-xhrio {:method          :get
                 :uri             (api/route (str "applications/" id))
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:application.entry.fetch/success]
                 :on-failure      [:application.entry.fetch/fail]}}))

(reg-event-fx
 :application.entry.fetch/success
 (fn [{:keys [db]} [_ result]]
   (l/log result)
   {:db (-> (model/application db result)
            model/toggle-loading)
    :dispatch [:application.entry.menu/determine-completeness]}))

(reg-event-fx
 :application.entry.fetch/fail
 (fn [{:keys [db]} [_ {:keys [response] :as error}]]
   (let [err-msg (or (:error response) "Failed to fetch application.")]
     (l/error "Failed to fetch application." error)
     {:db       (model/toggle-loading db)
      :dispatch [:notify/error err-msg]})))

;; =============================================================================
;; Menu

(reg-event-db
 :application.entry.menu/determine-completeness
 (fn [db _]
   (model/determine-tab-completeness db)))

(reg-event-db
 :application.entry.menu/select-tab
 (fn [db [_ new-tab]]
   (model/active-tab db new-tab)))

;; =====================================
;; Approval

(reg-event-db
 :application.entry.approval/select-community
 (fn [db [_ community]]
   (-> (model/select-community db community)
       (model/reset-email-content community)
       (model/initialize-security-deposit community))))

(reg-event-db
 :application.entry.approval.email-content/change
 (fn [db [_ new-content]]
   (model/email-content db new-content)))

(reg-event-db
 :application.entry.approval.deposit/change
 (fn [db [_ new-amount]]
   (model/security-deposit-amount db new-amount)))

(reg-event-fx
 :application.entry/approve
 (fn [{:keys [db]} [_ community-id]]
   (let [{:keys [email-content deposit-amount]} (get db root-db-key)]
     {:db         (model/toggle-approving db)
      :http-xhrio {:method          :post
                   :uri             (api/route (str "applications/" (model/current-id db) "/approve"))
                   :params          {:community-id   community-id
                                     :deposit-amount deposit-amount
                                     :email-content  email-content}
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:application.entry.approve/success]
                   :on-failure      [:application.entry.approve/failure]}})))

(reg-event-fx
 :application.entry.approve/success
 (fn [{:keys [db]} _]
   ;; Mark as approved and notify the user
   {:db         (-> db model/approved model/toggle-approving)
    :dispatch-n [[:notify/success "Successfully approved!"]
                 [:application.entry.menu/select-tab :move-in]]}))

(reg-event-fx
 :application.entry.approve/failure
 (fn [{:keys [db]} [_ {:keys [response] :as err}]]
   (let [err-msg (or (:error response) "Failed to approve. Please try again.")]
     (l/error "Error encountered while attempting approval." err) ; debug
     {:db       (model/toggle-approving db)
      :dispatch [:notify/error err-msg]})))
