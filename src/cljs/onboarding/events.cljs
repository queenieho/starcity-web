(ns onboarding.events
  (:require [day8.re-frame.http-fx]
            [onboarding.db :as db]
            [onboarding.prompts.events]
            [onboarding.routes :as routes]
            [re-frame.core :refer [reg-event-fx path]]
            [toolbelt.core :as tb]))

;; =============================================================================
;; Init
;; =============================================================================

(reg-event-fx
 :app/init
 (fn [{:keys [db]} _]
   {:db       db/default-value
    :dispatch [:app/bootstrap]}))

(def ^:private dev-payload
  {:deposit/method        {:complete true
                           :data     {:method "ach"}}
   :deposit.method/bank   {:complete true
                           :data     {:name           "Josh Lehman"
                                      :routing-number "110000000"
                                      :account-number "000123456879"}}
   :deposit.method/verify {:complete true
                           :data     {:amount-1 1 :amount-2 2}}
   :deposit/pay           {:complete    true
                           :rent-amount 2200
                           :data        {:method "partial"}}
   :services/moving       (let [d (.add (js/moment.) 2 "w")]
                            {:complete     true
                             :commencement d
                             :data         {:needed true
                                            :date   d
                                            :time   (-> d (.hours 12) (.minutes 0))}})
   :services/storage      {:data {:needed true}}})

;; Fetch the server-side progress
(reg-event-fx
 :app/bootstrap
 (fn [{:keys [db]} _]
   {:db             (assoc db :bootstrapping true)
    :dispatch-later [{:ms       1000
                      :dispatch [:app.bootstrap/success {:result dev-payload}]}]}))

;; On success, bootstrap the app database with server-side data
(reg-event-fx
 :app.bootstrap/success
 (fn [{:keys [db]} [_ {result :result}]]
   (let [db (db/bootstrap db result)]
     {:db    (assoc db :bootstrapping false)
      :route (routes/path-for (get-in db [:menu :active]))})))

;; =============================================================================
;; Routing
;; =============================================================================

;; Prevents flicker by checking if the application is currently being
;; bootstrapped. If so, do nothing. After bootstrap success, a `:route` effect
;; will be observed.
(reg-event-fx
 :app/route
 (fn [{:keys [db]} [_ keypath params]]
   (when-not (:bootstrapping db)
     (if (db/can-navigate-to? db keypath)
       {:db       (assoc-in db [:menu :active] keypath)
        :dispatch [:prompt/init keypath]}
       {:route         (routes/path-for (get-in db [:menu :default]))
        :alert/message {:type    :warning
                        :content "Step not available"}}))))
