(ns admin.accounts.events
  (:require [admin.accounts.db :as db]
            [admin.accounts.check-form.events]
            [admin.routes :as routes]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [re-frame.core :refer [path reg-event-db reg-event-fx]]
            [toolbelt.core :as tb]
            [plumbing.core :as plumbing]
            [cljs-time.coerce :as c]
            [clojure.string :as str]))

;; =============================================================================
;; Autocomplete
;; =============================================================================

;; =====================================
;; Search

(reg-event-fx
 :accounts.autocomplete/search
 [(path db/path)]
 (fn [{:keys [db]} [_ query]]
   (if-not (empty? query)
     {:dispatch-throttle {:id              :accounts.autocomplete/search
                          :dispatch        [:accounts.autocomplete/search* query]
                          :window-duration 600
                          :trailing?       true
                          :leading?        false}}
     {:db (db/reset-autocomplete-results db)})))

(reg-event-fx
 :accounts.autocomplete/search*
 [(path db/path)]
 (fn [{:keys [db]} [_ q]]
   {:http-xhrio {:method          :get
                 :uri             "/api/v1/admin/accounts/autocomplete"
                 :params          {:q q}
                 :response-format (ajax/transit-response-format)
                 :on-success      [:accounts.autocomplete.search/success]
                 :on-failure      [:accounts.autocomplete.search/failure]}}))

(reg-event-db
 :accounts.autocomplete.search/success
 [(path db/path)]
 (fn [db [_ {results :result :as res}]]
   (db/autocomplete-results db results)))

(reg-event-db
 :accounts.autocomplete.search/failure
 [(path db/path)]
 (fn [db [_ err]]
   (tb/error err)
   db))

;; =====================================
;; Select

(defn- email->account-id [db email]
  (:db/id
   (tb/find-by (comp #{email} :account/email) (db/autocomplete-results db))))

;; NOTE: The email needs to be passed in, not the id. Autocomplete
;; will take this value (visually, in the field), so it should
;; be human-legible.
(reg-event-fx
 :accounts.autocomplete/select
 [(path db/path)]
 (fn [{:keys [db]} [_ email]]
   (let [account-id (email->account-id db email)]
     {:db    (db/select-autocomplete-result db account-id)
      :route (routes/path-for :account :account-id account-id)})))

;; =============================================================================
;; Accounts Overview
;; =============================================================================

(reg-event-fx
 :accounts/navigate
 [(path db/path)]
 (fn [{:keys [db]} _]
   {:dispatch [:accounts/fetch-overview]}))

(reg-event-fx
 :accounts/fetch-overview
 [(path db/path)]
 (fn [{:keys [db]} _]
   {:db         (db/is-fetching-overview db)
    :http-xhrio {:method          :get
                 :uri             "/api/v1/admin/accounts/overview"
                 :response-format (ajax/transit-response-format)
                 :on-success      [:accounts.fetch-overview/success]
                 :on-failure      [:accounts.fetch-overview/failure]}}))

(reg-event-fx
 :accounts.fetch-overview/success
 [(path db/path)]
 (fn [{:keys [db]} [_ {result :result}]]
   {:db (db/done-fetching-overview db result)}))

(reg-event-fx
 :accounts.fetch-overview/failure
 [(path db/path)]
 (fn [{:keys [db]} [_ err]]
   {:db            (db/error-fetching-overview db err)
    :alert/message {:type    :error
                    :content "Failed to fetch accounts overview."}}))

(reg-event-db
 :accounts.overview.applicants/change-view
 [(path db/path)]
 (fn [db [_ new-view]]
   (assoc-in db [:overview :applicants/view] new-view)))

;; =============================================================================
;; Account Entry
;; =============================================================================

#_(defn- dispatches-for-page [page account-id]
  (cond-> [[:account/fetch account-id]]
    (= page :account/notes) (conj [:notes.account/fetch account-id])))

(reg-event-fx
 :account/navigate
 [(path db/path)]
 (fn [{:keys [db]} [_ page account-id]]
   {:db       (db/viewing-account-id db account-id)
    :dispatch [:account/fetch account-id]}))

(reg-event-fx
 :account.viewing/refresh
 [(path db/path)]
 (fn [{:keys [db]} _]
   {:dispatch [:account/fetch (:viewing db)]}))

(reg-event-fx
 :account/fetch
 [(path db/path)]
 (fn [{:keys [db]} [_ account-id]]
   {:db            (db/is-fetching-account db)
    :http-xhrio    {:method          :get
                    :uri             (str "/api/v1/admin/accounts/" account-id)
                    :response-format (ajax/transit-response-format)
                    :on-success      [:account.fetch/success]
                    :on-failure      [:account.fetch/failure]}}))

(reg-event-fx
 :account.fetch/success
 [(path db/path)]
 (fn [{:keys [db]} [_ {account :result}]]
   {:db (db/done-fetching-account db account)}))

(reg-event-fx
 :account.fetch/failure
 [(path db/path)]
 (fn [{:keys [db]} [_ err]]
   (tb/error err)
   {:db            (db/error-fetching-account db err)
    :alert/message {:type    :error
                    :content "Failed to fetch account!"}}))

;; =============================================================================
;; Approval
;; =============================================================================

;; =====================================
;; UI

(reg-event-db
 :approval/show
 [(path db/path)]
 (fn [db _]
   (db/show-approval db)))

(reg-event-db
 :approval/hide
 [(path db/path)]
 (fn [db _]
   (db/hide-approval db)))

;; =====================================
;; Updates

(defn- approval-data [db]
  (-> db :approval (select-keys [:community :move-in :unit :license])))

(defmulti approval-update (fn [db k v] k))

(defmethod approval-update :default [_ k v]
  (println "Unhandled update:" k v)
  {})

(defmethod approval-update :community
  [db _ property-id]
  (let [new-db (update db :approval #(-> (assoc % :community property-id)
                                         (dissoc :unit)))]
    {:db       new-db
     :dispatch [::fetch-units (:approval new-db)]}))

(defmethod approval-update :license
  [db _ license-id]
  (let [approval (assoc (approval-data db) :license license-id)]
    {:db       (update db :approval merge approval)
     :dispatch [::fetch-units approval]}))

(defmethod approval-update :move-in
  [db _ move-in]
  (let [approval (assoc (approval-data db) :move-in move-in)]
    {:db       (update db :approval merge approval)
     :dispatch [::fetch-units approval]}))

(defmethod approval-update :unit
  [db _ unit-id]
  {:db (assoc-in db [:approval :unit] unit-id)})

(reg-event-fx
 :approval/update
 [(path db/path)]
 (fn [{:keys [db]} ev]
   (apply approval-update db (rest ev))))

(reg-event-fx
 ::fetch-units
 [(path db/path)]
 (fn [{:keys [db]} [_ {:keys [community move-in license]}]]
   ;; Only fetch units if both a `community` AND `move-in` date are selected.
   (when (and community move-in license)
     {:db            (db/is-fetching-units db)
      :alert/message {:type :loading :content "Loading..." :duration :indefinite}
      :http-xhrio    {:method          :get
                      :uri             (str "/api/v1/admin/properties/" community "/units")
                      :params          {:available-by (c/to-long move-in)
                                        :license      license}
                      :response-format (ajax/transit-response-format)
                      :on-success      [::fetch-units-success]
                      :on-failure      [::fetch-units-failure]}})))

(reg-event-fx
 ::fetch-units-success
 [(path db/path)]
 (fn [{:keys [db]} [_ {units :result}]]
   {:db                 (db/done-fetching-units db units)
    :alert.message/hide true}))

(reg-event-fx
 ::fetch-units-failure
 [(path db/path)]
 (fn [{:keys [db]} [_ error]]
   (tb/error "error fetching units:" error)
   {:db                 (db/error-fetching-units db)
    :alert.message/hide true}))

;; =====================================
;; Approve

(reg-event-fx
 :approval/approve
 [(path db/path)]
 (fn [{:keys [db]} _]
   (let [{:keys [community license move-in unit]} (:approval db)
         account-id                               (:viewing db)]
     {:db         (db/is-approving db)
      :http-xhrio {:method          :post
                   :uri             (str "/api/v1/admin/accounts/" account-id "/approve")
                   :params          {:license-id license
                                     :move-in    move-in
                                     :unit-id    unit}
                   :format          (ajax/transit-request-format)
                   :response-format (ajax/transit-response-format)
                   :on-success      [::approve-success]
                   :on-failure      [::approve-failure]}})))

(reg-event-fx
 ::approve-success
 [(path db/path)]
 (fn [{:keys [db]} _]
   {:db            (db/done-approving db)
    :dispatch-n    [[:approval/hide]
                    [:account/fetch (:viewing db)]]
    :alert/message {:type    :success
                    :content "Approved!"}}))

(reg-event-fx
 ::approve-failure
 [(path db/path)]
 (fn [{:keys [db]} [_ {res :response :as error}]]
   (tb/error error)
   {:db           (db/done-approving db)
    :alert/notify {:type    :error
                   :title   "Failed to approve!"
                   :content (or (:message res)
                                "An unknown error occurred.")}}))

;; =============================================================================
;; Navigation
;; =============================================================================

(reg-event-fx
 :account.subnav/navigate-to
 [(path db/path)]
 (fn [{:keys [db]} [_ to]]
   (let [account-id (db/viewing-account-id db)]
     {:route (routes/path-for to :account-id account-id)})))
