(ns apply.prompts.events
  (:require [apply.routes :refer [prompt-uri]]
            [apply.prompts.models :as prompts]
            [apply.notifications :as n]
            [re-frame.core :refer [reg-event-fx
                                   reg-event-db]]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [starcity.log :as l]))

(def ^:private next-uri
  "Produce the uri of the next prompt as defined by `prompts`."
  (comp prompt-uri prompts/next))

;; TODO: refactor to multimethod
(defn- save-request
  "Construct a save request."
  [prompt-key params & [on-success]]
  (let [overrides (when (= prompt-key :personal/income)
                    {:uri  "/api/v1/apply/verify-income"
                     :body params})]
    (merge {:method          :post
            :uri             "/api/v1/apply/update"
            :params          {:data params
                              :path [(namespace prompt-key) (name prompt-key)]}
            :format          (ajax/json-request-format)
            :response-format (ajax/json-response-format {:keywords? true})
            :on-failure      [:prompt.save/fail]
            :on-success      (or on-success [:prompt.save/success])}
           overrides)))

;; Event triggered by "advancing" through the application process.
;; First initiate a save request, and then on success initiate a browser url
;; change to the uri of the next prompt.
(reg-event-fx
 :prompt/next
 (fn [{:keys [db] :as cofx} _]
   ;; get the local data
   (let [data (get-in db [(:prompt/current db) :local])]
     {:db         (assoc db :prompt/loading true)
      :http-xhrio (save-request (:prompt/current db) data [:prompt.next/success data])})))

(reg-event-fx
 :prompt.next/success
 (fn [{:keys [db]} [_ data result]]
   (let [prompt-key (:prompt/current db)]
     {:route    (next-uri prompt-key)
      :dispatch [:app/parse result]
      :db       (assoc db :prompt/loading false)})))      ; no longer loading

(reg-event-fx
 :prompt/save
 (fn [{:keys [db] :as cofx} [_ data]]
   ;; TODO:
   ))

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
       default))))

(reg-event-fx
 :prompt.save/fail
 (fn [{:keys [db]} [_ err]]
   (l/error err)
   {:dispatch [:app/notify (errors-for err)]
    :db       (assoc db :prompt/loading false)}))
