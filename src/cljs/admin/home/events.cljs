(ns admin.home.events
  (:require [admin.home.db :as db]
            [day8.re-frame.http-fx]
            [re-frame.core :refer [reg-event-db
                                   reg-event-fx
                                   path]]
            [plumbing.core :as plumbing]
            [ajax.core :as ajax]
            [cljsjs.moment]
            [toolbelt.core :as tb]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- format-moment [m]
  (.format m "YYYY-MM-DD"))

;; =============================================================================
;; Initialize
;; =============================================================================

(reg-event-fx
 :home/initialize
 [(path db/path)]
 (fn [{:keys [db]} _]
   (let [{:keys [from to]} (:controls db)]
     {:dispatch-n [[:home.metrics/fetch from to]
                   [:home.referrals/fetch from to]]})))

;; =============================================================================
;; Controls
;; =============================================================================

(reg-event-db
 :home.controls/update
 [(path db/path)]
 (fn [db [_ k v]]
   (assoc-in db [:controls k] v)))

;; =============================================================================
;; Metrics
;; =============================================================================

(reg-event-fx
 :home.metrics/update!
 [(path db/path)]
 (fn [{:keys [db]} [_ {:keys [from to]}]]
   {:dispatch-n [[:home.metrics/fetch from to]
                 [:home.referrals/fetch from to]]}))

(reg-event-fx
 :home.metrics/fetch
 [(path db/path)]
 (fn [{:keys [db]} [_ from to]]
   {:db         (assoc db :metrics/loading true)
    :http-xhrio {:method          :get
                 :uri             "/api/v1/admin/metrics"
                 :params          {:pstart (format-moment from)
                                   :pend   (format-moment to)}
                 :response-format (ajax/transit-response-format)
                 :on-success      [:home.metrics.fetch/success]
                 :on-failure      [:home.metrics.fetch/failure]}}))

(reg-event-db
 :home.metrics.fetch/success
 [(path db/path)]
 (fn [db [_ {result :result}]]
   (-> (merge db result)
       (assoc :metrics/loading false))))

(reg-event-fx
 :home.metrics.fetch/failure
 [(path db/path)]
 (fn [{:keys [db]} [_ {res :response :as error}]]
   (tb/error error)
   {:db            (assoc db :metrics/loading false)
    :alert/message {:type :error :content "Failed to fetch metrics."}}))

;; =============================================================================
;; Referrals
;; =============================================================================

(reg-event-fx
 :home.referrals/fetch
 [(path db/path)]
 (fn [{:keys [db]} [_ from to]]
   {:db         (assoc db :referrals/loading true)
    :http-xhrio {:method          :get
                 :uri             "/api/v1/admin/referrals"
                 :params          {:pstart (format-moment from)
                                   :pend   (format-moment to)}
                 :format          (ajax/transit-request-format)
                 :response-format (ajax/transit-response-format)
                 :on-success      [:home.referrals.fetch/success]
                 :on-failure      [:home.referrals.fetch/success]}}))

(reg-event-db
 :home.referrals.fetch/success
 [(path db/path)]
 (fn [db [_ {result :result}]]
   (-> (assoc db :referrals result)
       (assoc :referrals/loading false))))

(reg-event-fx
 :home.referrals.fetch/failure
 [(path db/path)]
 (fn [{:keys [db]} [_ {res :response :as error}]]
   (tb/error error)
   {:db            (assoc db :referrals/loading false)
    :alert/message {:type    :error
                    :content (first (:errors res))}}))
