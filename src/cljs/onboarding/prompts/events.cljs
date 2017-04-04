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

;; We'll need this for:
;; 1. Fetching rent amount on :deposit/pay
;; 2. TBD

(reg-event-fx
 :prompt/init
 (fn [{:keys [db]} [_ keypath]]
   {:db       (if-let [prompt (get db keypath)]
                db
                (assoc db keypath {:initialized false}))
    :dispatch [:prompt/fetch keypath]}))

(reg-event-fx
 :prompt/fetch
 (fn [{:keys [db]} [_ keypath]]
   {:db             db
    :dispatch-later [{:ms 1000 :dispatch [:prompt.fetch/success keypath]}]}))

(reg-event-fx
 :prompt.fetch/success
 (fn [{:keys [db]} [_ keypath {result :result}]]
   {:db (assoc-in db [keypath :initialized] true)}))

;; =============================================================================
;; Navigation
;; =============================================================================

;; =============================================================================
;; Advancement

(def ^:private deposit-modal-content
  (r/as-element
   [:p "By pressing the " [:b "Pay Now"] " button below, I authorize Starcity to
   electronically debit my account and,if necessary, electronically credit my
   account to correct erroneous debits."]))

(def ^:private bank-info-modal-content
  (r/as-element
   [:div
    [:p {:dangerouslySetInnerHTML {:__html "Over the next <b>24-48 hours</b>,
     two small deposits will be made in your account with the statement
     description <b>VERIFICATION</b> &mdash; enter them below to verify that you
     are the owner of this bank account."}}]
    [:br]
    [:p "TODO:"]]))

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

(reg-event-fx
 :deposit.method.bank/submit
 (fn [{:keys [db]} [_ keypath]]
   (let [{:keys [name routing-number account-number]} (get-in db [keypath :data])]
     (tb/log (get-in db [keypath :data]))
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
