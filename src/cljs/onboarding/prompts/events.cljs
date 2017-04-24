(ns onboarding.prompts.events
  (:require [onboarding.db :as db]
            [re-frame.core :refer [reg-event-fx
                                   reg-event-db
                                   path]]
            [starcity.fx.stripe]
            [onboarding.routes :as routes]
            [reagent.core :as r]
            [ajax.core :as ajax]
            [toolbelt.core :as tb]))

;; =============================================================================
;; Prompt-specific Initialization
;; =============================================================================

(defmulti init-prompt (fn [db keypath] keypath))

(defmethod init-prompt :default
  [db _]
  db)

(defmethod init-prompt :services/moving
  [db _]
  (let [move-in (aget js/account "move-in")
        moving  {:commencement move-in
                 :data         {:date move-in}}]
    (-> (assoc-in db [:services/moving :commencement] move-in)
        (update-in [:services/moving :data :date] #(or % move-in)))))

(defn- enforce-seen
  [db keypath]
  (let [seen (get-in db [keypath :data :seen])]
    (if seen
      db
      (-> (assoc-in db [keypath :data :seen] true)
          (assoc-in [keypath :dirty] true)))))

;; Only enforces that this prompt is seen at least once
(defmethod init-prompt :services/storage [db keypath]
  (enforce-seen db keypath))

(defmethod init-prompt :services/customize [db keypath]
  (enforce-seen db keypath))

(defmethod init-prompt :services/cleaning [db keypath]
  (enforce-seen db keypath))

(defmethod init-prompt :services/upgrades [db keypath]
  (enforce-seen db keypath))

(reg-event-fx
 :prompt/init
 (fn [{:keys [db]} [_ keypath]]
   {:db       (init-prompt db keypath)
    #_(if-let [prompt (get db keypath)]
        db
        (assoc db keypath {:initialized false}))
    ;; :dispatch [:prompt/fetch keypath]
    }))

;; =============================================================================
;; Navigation
;; =============================================================================

;; =============================================================================
;; Advancement

(def ^:private deposit-modal-content
  (r/as-element
   [:p "By pressing the " [:b "Pay Now"] " button below, I authorize Starcity to
   electronically debit my account and, if necessary, electronically credit my
   account to correct erroneous debits."]))

(def ^:private bank-info-modal-content
  (r/as-element
   [:div
    [:p {:dangerouslySetInnerHTML {:__html "Over the next <b>24-48 hours</b>,
     two small deposits will be made in your account with the statement
     description <b>VERIFICATION</b> &mdash; enter them in the next step to
     verify that you are the owner of this bank account."}}]]))

(defmulti begin-save
  "Used to determine how to proceed after a successful press of the 'Continue'
  button. In most cases we'll simply send the form data directly to the
  server (see the `:default` case), but in some cases other client-side steps
  may need to be performed."
  (fn [db keypath] keypath))

(defmethod begin-save :default [db keypath]
  {:dispatch [:prompt/save keypath (get-in db [keypath :data])]})

(defmethod begin-save :deposit/pay [db keypath]
  {:ant.modal/confirm {:title   "Payment Confirmation"
                       :content deposit-modal-content
                       :ok-text "Pay Now"
                       :on-ok   [:prompt/save keypath (get-in db [keypath :data])]}})

(defmethod begin-save :deposit.method/bank [_ keypath]
  {:ant.modal/info {:title   "Microdeposit Lag-time"
                    :content bank-info-modal-content
                    :on-ok   [:deposit.method.bank/submit keypath]}})

(defn- remove-catalogue [db keypath]
  {:dispatch [:prompt/save keypath (-> (get-in db [keypath :data])
                                       (dissoc :catalogue))]})

(defmethod begin-save :services/storage [db keypath]
  (remove-catalogue db keypath))

(defmethod begin-save :services/customize [db keypath]
  (remove-catalogue db keypath))

(defmethod begin-save :services/cleaning [db keypath]
  (remove-catalogue db keypath))

(defmethod begin-save :services/upgrades [db keypath]
  (remove-catalogue db keypath))

(reg-event-fx
 :deposit.method.bank/submit
 (fn [{:keys [db]} [_ keypath]]
   (let [{:keys [name routing-number account-number]} (get-in db [keypath :data])]
     {:db (db/pre-save (assoc db :saving true) keypath)
      :stripe.bank-account/create-token
      {:country             "US"
       :currency            "USD"
       :account-holder-type "individual"
       :key                 (.-key js/stripe)
       :account-holder-name name
       :routing-number      routing-number
       :account-number      account-number
       :on-success          [:stripe.bank-account.create-token/success keypath]
       :on-failure          [:stripe.bank-account.create-token/failure]}})))

(reg-event-fx
 :stripe.bank-account.create-token/success
 (fn [_ [_ keypath res]]
   {:dispatch [:prompt/save keypath {:stripe-token (:id res)}]}))

(reg-event-fx
 :stripe.bank-account.create-token/failure
 (fn [{:keys [db]} [_ error]]
   (tb/error "Failed to create Stripe Token:" error)
   {:db           (assoc db :saving false)
    :alert/notify {:type     :error
                   :duration 8
                   :title    "Error!"
                   :content  "Something went wrong while submitting your bank information. Please check the account and routing number and try again."}}))

(reg-event-fx
 :prompt/continue
 (fn [{:keys [db]} [_ keypath]]
   (let [dirty (get-in db [keypath :dirty])]
     (if dirty
       (begin-save db keypath)
       {:route (routes/path-for (db/next-prompt db keypath))}))))

(reg-event-fx
 :prompt/save
 (fn [{:keys [db]} [_ keypath data]]
   {:db         (db/pre-save (assoc db :saving true) keypath)
    :http-xhrio {:method          :post
                 :uri             "/api/v1/onboarding"
                 :params          {:step keypath :data data}
                 :format          (ajax/transit-request-format)
                 :response-format (ajax/transit-response-format)
                 :on-success      [:prompt.save/success keypath]
                 :on-failure      [:prompt.save/failure keypath]}}))

(reg-event-fx
 :prompt.save/success
 (fn [{:keys [db]} [_ keypath {result :result}]]
   (let [;result (get-in db [keypath :data]) ; for development
         ;; =================
         db     (-> (assoc db :saving false)
                    (assoc-in [keypath :dirty] false)
                    (update-in [:menu :complete] conj keypath)
                    (assoc-in [keypath :complete] true)
                    (db/post-save keypath (:data result)))]
     {:db    db
      :route (routes/path-for (db/next-prompt db keypath))})))

(reg-event-fx
 :prompt.save/failure
 (fn [{:keys [db]} [_ keypath error]]
   (tb/error error)
   (-> (if-let [errors (get-in error [:response :errors])]
         {:alert/notify {:type    :error
                         :title   "Error!"
                         :content (first errors)}}
         {:alert/message {:type    :error
                          :content "Yikes! A server-side error was encountered."}})
       (merge {:db (assoc db :saving false)}))))

;; =============================================================================
;; Retreat

(reg-event-fx
 :prompt/previous
 (fn [{:keys [db]} [_ keypath]]
   {:route (routes/path-for (db/previous-prompt db keypath))}))

;; =============================================================================
;; Updates
;; =============================================================================

(defmulti update-prompt (fn [db keypath k v] keypath))

(defmethod update-prompt :default
  [db keypath k v]
  (assoc-in db [keypath :data k] v))

(reg-event-db
 :prompt/update
 (fn [db [_ keypath k v]]
   (update-prompt (assoc-in db [keypath :dirty] true) keypath k v)))

(reg-event-fx
 :prompt.orders/select
 (fn [{:keys [db]} [_ keypath {:keys [service fields variants] :as item}]]
   (let [defaults {:quantity 1 :desc "" :variants (:id (first variants))}
         init     (reduce #(assoc %1 (:key %2) (get defaults (:type %2))) {} fields)]
     {:dispatch [:prompt.orders/update keypath [service init]]})))

(reg-event-fx
 :prompt.orders/update
 (fn [{:keys [db]} [_ keypath [service params]]]
   (let [orders   (-> (get-in db [keypath :data :orders])
                      (assoc service params))]
     {:dispatch [:prompt/update keypath :orders orders]})))

(reg-event-fx
 :prompt.orders/remove
 (fn [{:keys [db]} [_ keypath service]]
   (let [orders (-> (get-in db [keypath :data :orders])
                    (dissoc service))]
     {:dispatch [:prompt/update keypath :orders orders]})))
