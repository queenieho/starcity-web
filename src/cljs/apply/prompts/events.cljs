(ns apply.prompts.events
  (:require [apply.routes :refer [prompt-uri]]
            [apply.notifications :as n]
            [re-frame.core :refer [reg-event-fx
                                   reg-event-db]]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [starcity.log :as l])
  (:refer-clojure :exclude [next]))

;; A mapping of <current-prompt> to <next-prompt> to determine where to go after
;; the `Next` button has been pressed.

;; NOTE: It would be nice if we could invert the map to use for previous buttons
;; too, instead of needing to supply this at the view level
(def ^:private prompts
  {:logistics/communities  :logistics/term
   :logistics/term         :logistics/move-in-date
   :logistics/move-in-date :logistics/pets
   :logistics/pets         :personal/phone-number

   :personal/phone-number :personal/background
   :personal/background   :personal/income
   :personal/income       :community/why-starcity

   :community/why-starcity    :community/about-you
   :community/about-you       :community/communal-living
   :community/communal-living :overview/welcome})

(defn- next-uri
  "Produce the uri of the next prompt as defined by `prompts`."
  [prompt-key]
  (prompt-uri (get prompts prompt-key)))

(defn- files->form-data [files]
  (let [form-data (js/FormData.)]
    (doseq [file-key (.keys js/Object files)]
      (let [file (aget files file-key)]
        (.append form-data "files[]" file (.-name file))))
    form-data))

(defn- save-request
  "Construct a save request."
  [prompt-key params & [on-success]]
  (let [overrides (when (= prompt-key :personal/income)
                    {:uri  "/api/v1/apply/verify-income"
                     :body (files->form-data params)})]
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
 (fn [{:keys [db] :as cofx} [_ data]]
   {:db         (assoc db :prompt/loading true)
    :http-xhrio (save-request (:prompt/current db) data [:prompt.next/success data])}))

(reg-event-fx
 :prompt.next/success
 (fn [{:keys [db]} [_ data result]]
   (let [prompt-key (:prompt/current db)]
     (l/log result)
     {:route (next-uri prompt-key)
      :db    (assoc db prompt-key data :prompt/loading false)})))

(reg-event-fx
 :prompt/save
 (fn [{:keys [db] :as cofx} [_ data]]
   ;; TODO:
   ))

(defn- errors-for [error]
  (let [default "Whoops! Something went wrong."]
    (n/error
     (case (:status error)
       404 default
       default))))

(reg-event-fx
 :prompt.save/fail
 (fn [{:keys [db]} [_ err]]
   (l/error err)
   {:dispatch [:app/notify (errors-for err)]
    :db       (assoc db :prompt/loading false)}))
