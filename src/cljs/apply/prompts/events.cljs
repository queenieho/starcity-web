(ns apply.prompts.events
  (:require [apply.routes :refer [prompt-uri]]
            [apply.prompts.models :as prompts]
            [apply.notifications :as n]
            [re-frame.core :refer [reg-event-fx
                                   reg-event-db]]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [starcity.log :as l]))

;; =============================================================================
;; Helpers
;; =============================================================================

;; TODO: refactor to multimethod
(defn- save-request
  "Construct a save request."
  [prompt-key params on-success on-failure]
  (let [
        overrides (when (= prompt-key :personal/income)
                    {:uri  "/api/v1/apply/verify-income"
                     :body params})]
    (merge {:method          :post
            :uri             "/api/v1/apply/update"
            :params          {:data params
                              :path [(namespace prompt-key) (name prompt-key)]}
            :format          (ajax/json-request-format)
            :response-format (ajax/json-response-format {:keywords? true})
            :on-failure      on-failure
            :on-success      on-success}
           overrides)))

;; the bouncer errors come in the form [<error key> [<vector of errors>]]
(defn- parse-error [error]
  (if (sequential? error)
    (first (second error))
    error))

(defn- errors-for [error]
  (let [default "Whoops! Something went wrong. Please try again."]
    (n/error
     (case (:status error)
       400 (first (map parse-error (-> error :response :errors)))
       422 (-> error :response :errors first)
       default))))

;; =============================================================================
;; Navigation
;; =============================================================================

;; On navigation, update the current prompt and clear any notifications
(reg-event-fx
 :prompt/nav
 (fn [{:keys [db]} [_ prompt-key]]
   {:db       (assoc db :prompt/current prompt-key)
    :dispatch [:notification/clear-all]}))

;; =============================================================================
;; Next
;; =============================================================================

(def ^:private next-uri
  "Produce the uri of the next prompt as defined by `prompts`."
  (comp prompt-uri prompts/next))

(defn- next-prompt-uri
  "Produce the URI of the prompt. This is dependent upon the current prompt and
  whether or not all of the prompts are complete."
  [prompt all-prompts-complete?]
  (if (prompts/final? prompt)
    (if all-prompts-complete?
      (next-uri prompt)
      (prompt-uri :overview/welcome))
    (next-uri prompt)))

(defmulti next-prompt (fn [db _] (:prompt/current db)))

(defmethod next-prompt :finish/pay [db _]
  {:dispatch [:finish/begin-checkout]})

(defmethod next-prompt :default [db _]
  (let [curr-prompt            (:prompt/current db)
        {:keys [local remote]} (get db curr-prompt)]
    (if (not= local remote)
      ;; Update data on server since it has change
      {:db         (assoc db :prompt/loading true)
       :http-xhrio (save-request curr-prompt local
                                 [:prompt.next/success]
                                 [:prompt.next/fail])}
      ;; just advance, since there's no change
      {:route (next-prompt-uri curr-prompt (prompts/complete db))})))

;; Event triggered by "advancing" through the application process.
;; First initiate a save request, and then on success initiate a browser url
;; change to the uri of the next prompt.
(reg-event-fx
 :prompt/next
 (fn [{:keys [db]} evt]
   (next-prompt db evt)))

(reg-event-fx
 :prompt.next/success
 (fn [{:keys [db]} [_ result]]
   (let [prompt-key (:prompt/current db)]
     {:route    (next-prompt-uri prompt-key (:payment-allowed result))
      :dispatch [:app/parse result]
      :db       (assoc db :prompt/loading false)})))      ; no longer loading

(reg-event-fx
 :prompt.next/fail
 (fn [{:keys [db]} [_ err]]
   (l/error err)
   {:dispatch [:app/notify (errors-for err)]
    :db       (assoc db :prompt/loading false)}))

;; =============================================================================
;; Save
;; =============================================================================

(reg-event-fx
 :prompt/save
 (fn [{:keys [db] :as cofx} [_ data]]
   (let [curr-prompt            (:prompt/current db)
         {:keys [local remote]} (get db curr-prompt)]
     {:db         (assoc db :prompt/saving true)
      :http-xhrio (save-request curr-prompt local
                                [:prompt.save/success]
                                [:prompt.save/fail])})))

(reg-event-fx
 :prompt.save/success
 (fn [{:keys [db]} [_ result]]
   {:db       (assoc db :prompt/saving false)
    :dispatch [:app/parse result]}))

(reg-event-fx
 :prompt.save/fail
 (fn [{:keys [db]} [_ err]]
   (l/error err)
   {:dispatch [:app/notify (errors-for err)]
    :db       (assoc db :prompt/saving false)}))

;; =============================================================================
;; Help
;; =============================================================================

(reg-event-db
 :prompt.help/toggle
 (fn [db _]
   (update-in db [:prompt/help :showing] not)))

(reg-event-db
 :prompt.help/change
 (fn [db [_ new-question]]
   (assoc-in db [:prompt/help :question] new-question)))

(reg-event-fx
 :prompt.help/send
 (fn [{:keys [db]} [_]]
   (let [question (get-in db [:prompt/help :question])
         prompt   (:prompt/current db)]
     {:db         (assoc db :prompt.help/loading true)
      :http-xhrio {:method          :post
                   :uri             "/api/v1/apply/help"
                   :params          {:question  question
                                     :sent-from [(namespace prompt) (name prompt)]}
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:prompt.help/success]
                   :on-failure      [:prompt.help/failure]}})))

(def ^:private help-fail-msg
  "Something went wrong while sending your message. Please try again.")

(reg-event-fx
 :prompt.help/failure
 (fn [{:keys [db]} [_ err]]
   (l/log err)
   {:db         (assoc-in db [:prompt/help :loading] false)
    :dispatch-n [[:app/notify (n/error help-fail-msg)]
                 [:prompt.help/toggle]]}))

(def ^:private help-success-msg
  "Thanks for sending a message! I'll respond soon by email.")

(reg-event-fx
 :prompt.help/success
 (fn [{:keys [db]} _]
   {:db             (-> (assoc-in db [:prompt/help :question] "")
                        (assoc-in [:prompt/help :loading] false))
    :dispatch-later [{:ms 4000 :dispatch [:notification/clear-all]}]
    :dispatch-n     [[:app/notify (n/success help-success-msg)]
                     [:prompt.help/toggle]]}))
