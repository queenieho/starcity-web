(ns admin.application.entry.events
  (:require-macros [hiccups.core :refer [html]])
  (:require [admin.application.entry.db :refer [root-db-key]]
            [admin.application.entry.model :as model]
            [hiccups.runtime :as hiccupsrt]
            [re-frame.core :refer [reg-event-fx
                                   reg-event-db]]
            [day8.re-frame.http-fx]
            [starcity.log :as l]
            [admin.api :as api]
            [ajax.core :as ajax]))

(defn- set-curr-app [db id]
  (assoc-in db [root-db-key :current-id] id))

(defn- set-active-tab [db tab]
  (assoc-in db [root-db-key :active-tab] tab))

(reg-event-fx
 :nav/application
 (fn [{:keys [db]} [_ id]]
   ;; TODO: Set active tab to initial...that'll need to come from db for this to
   ;; work
   {:db       (-> (assoc db :route :application/entry)
                  (set-curr-app id))
    :dispatch [:application.entry/fetch id]}))

(defn- toggle-loading [db]
  (update-in db [root-db-key :loading] not))

(reg-event-fx
 :application.entry/fetch
 (fn [{:keys [db]} [_ id]]
   {:db         (toggle-loading db)
    :http-xhrio {:method          :get
                 :uri             (api/route (str "applications/" id))
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:application.entry.fetch/success]
                 :on-failure      [:application.entry.fetch/fail]}}))

(reg-event-db
 :application.entry.fetch/success
 (fn [db [_ result]]
   (-> (assoc-in db [root-db-key :applications (:id result)] result)
       toggle-loading)))

(reg-event-db
 :application.entry.fetch/fail
 (fn [db [_ err]]
   (l/error err)
   ;; TODO: dispatch :notify/error
   (toggle-loading db)))

;; Handle a change in current tab
(reg-event-db
 :application.entry/view-tab
 (fn [db [_ new-tab]]
   (set-active-tab db new-tab)))

;; =====================================
;; Approval

(defn- curr-app-id [db]
  (get-in db [root-db-key :current-id]))

(defn- toggle-approving [db]
  (update-in db [root-db-key :approving] not))

(defn- select-community [db community]
  (assoc-in db [root-db-key :selected-community] community))

(defn- reset-email-content [db new-content]
  (assoc-in db [root-db-key :email-content] new-content))

(defn- set-initial-security-deposit-amount [db community]
  (let [application    (model/application db)
        amount         (model/initial-deposit-amount application community)]
    (assoc-in db [root-db-key :deposit-amount] amount)))

(defn- reset-security-deposit-amount [db amount]
  (assoc-in db [root-db-key :deposit-amount] amount))

(defn- internal-name->name
  [selected communities]
  (-> (filter #(= selected (:property/internal-name %)) communities) first :property/name))

(defn- email-content [full-name community-name]
  (let [first-name     (first (clojure.string/split full-name #" "))
        onboarding-url (str "https://" (.. js/window -location -hostname) "/onboarding")]
    (html
     [:p (str "Hi " first-name ",")]
     [:p "We've processed your application and you've been qualified to join the Starcity community at "
      [:strong community-name] "!"]
     [:p "INSERT CUSTOM MESSAGE HERE"]
     [:p "Here are next steps:"]
     [:ol
      [:li "Schedule a tour by replying to me here with times that work for you."]
      [:li "After your tour, we'll send you a license agreement and you can pay your security deposit "
       [:a {:href onboarding-url} "here"] ". If you've laready toured with us or would like to secure your spot without a tour, you can pay now."]]
     [:p "We are excited to have you join the Starcity community and look forward to making you at home."]
     [:p "Best," [:br] "Mo"]

     [:p "Mo Sakrani" [:br]
      "516.749.0046" [:br]
      [:a {:href "mailto:mo@joinstarcity.com"} "mo@joinstarcity.com"]])))

(defn- update-email-content [db selected-community]
  (let [{:keys [current-id applications]} (get db root-db-key)
        communities                       (get-in applications [current-id :properties])
        full-name                         (get-in applications [current-id :name])
        community-name                    (internal-name->name selected-community communities)]
    (reset-email-content db (email-content full-name community-name))))

(reg-event-db
 :application.entry.approval/select-community
 (fn [db [_ community]]
   ;; Select community
   (-> (select-community db community)
       (update-email-content community)
       (set-initial-security-deposit-amount community))))

(reg-event-db
 :application.entry.approval.email-content/change
 (fn [db [_ new-content]]
   (reset-email-content db new-content)))

(reg-event-db
 :application.entry.approval.deposit/change
 (fn [db [_ deposit-amount]]
   (reset-security-deposit-amount db deposit-amount)))

(reg-event-fx
 :application.entry/approve
 (fn [{:keys [db]} [_ community-id]]
   (let [{:keys [email-content deposit-amount]} (get db root-db-key)]
     {:db         (toggle-approving db)
      :http-xhrio {:method          :post
                   :uri             (api/route (str "applications/" (curr-app-id db) "/approve"))
                   :params          {:community-id   community-id
                                     :deposit-amount deposit-amount
                                     :email-content  email-content}
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:application.entry.approve/success]
                   :on-failure      [:application.entry.approve/fail]}})))

(defn- set-approved [db]
  (assoc-in db [root-db-key :applications (curr-app-id db) :approved] true))

(reg-event-fx
 :application.entry.approve/success
 (fn [{:keys [db]} _]
   ;; Mark as approved and notify the user
   {:db       (-> db set-approved toggle-approving)
    :dispatch [:notify/success "Successfully approved!"]}))

(reg-event-fx
 :application.entry.approve/fail
 (fn [{:keys [db]} [_ {:keys [response] :as err}]]
   (let [err-msg (or (:error response) "Failed to approve. Please try again.")]
     (l/error "Error encountered while attempting approval." err) ; debug
     {:db       (toggle-approving db)
      :dispatch [:notify/error err-msg]})))
