(ns onboarding.prompts.events
  (:require [onboarding.db :as db]
            [re-frame.core :refer [reg-event-fx
                                   reg-event-db
                                   path]]
            [onboarding.routes :as routes]
            [reagent.core :as r]))

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

(def ^:private modals
  {:deposit/pay {:fx      :ant.modal/confirm
                 :title   "Payment Confirmation"
                 :content deposit-modal-content
                 :ok-text "Pay Now"
                 :on-ok   [:prompt/save :deposit/pay]}

   :deposit.method/bank {:fx      :ant.modal/info
                         :title   "Microdeposit Lag-time"
                         :content bank-info-modal-content
                         :on-ok   [:prompt/save :deposit.method/bank]}})

(defn- has-modal?
  [keypath]
  (contains? modals keypath))

(reg-event-fx
 :prompt/continue
 (fn [{:keys [db]} [_ keypath]]
   (let [dirty (get-in db [keypath :dirty])]
     (cond
       (and dirty (has-modal? keypath))
       (let [modal (modals keypath)]
         {(:fx modal) modal})

       dirty
       {:dispatch [:prompt/save keypath]}

       :otherwise
       {:route (routes/path-for (db/next-prompt db keypath))}))))

(reg-event-fx
 :prompt/save
 (fn [{:keys [db]} [_ keypath]]
   {:db             (db/pre-save (assoc db :saving true) keypath)
    ;; TODO: HTTP
    :dispatch-later [{:ms 1000 :dispatch [:prompt.save/success keypath]}]}))

(reg-event-fx
 :prompt.save/success
 (fn [{:keys [db]} [_ keypath {result :result}]]
   (let [result (get-in db [keypath :data]) ; NOTE: for development only!
         ;; =================
         db     (-> (assoc db :saving false)
                    (assoc-in [keypath :dirty] false)
                    (update-in [:menu :complete] conj keypath)
                    (assoc-in [keypath :complete] true)
                    (db/post-save keypath result))]
     {:db    db
      :route (routes/path-for (db/next-prompt db keypath))})))

;; TODO: :prompt.save/failure

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
