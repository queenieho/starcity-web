(ns mars.activity.events
  (:require [re-frame.core :refer [reg-event-db
                                   reg-event-fx
                                   path]]
            [day8.re-frame.http-fx]
            [mars.activity.db :as db]
            [mars.api :as api]
            [ajax.core :as ajax]
            [starcity.log :as l]))

(reg-event-fx
 :activity/bootstrap
 [(path db/path)]
 (fn [_ _]
   {:dispatch [:activity.feed/fetch]}))

(reg-event-fx
 :activity.feed/fetch
 [(path db/path)]
 (fn [{:keys [db]} _]
   {:db         (db/set-feed-loading db true)
    :http-xhrio {:uri             (api/route "/news")
                 :method          :get
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:activity.feed.fetch/success]
                 :on-failure      [:activity.feed.fetch/failure]}}))

(reg-event-db
 :activity.feed.fetch/success
 [(path db/path)]
 (fn [db [_ {news :news}]]
   (-> (db/set-feed-loading db false)
       (db/set-feed news))))

(reg-event-db
 :activity.feed.fetch/failure
 [(path db/path)]
 (fn [db [_ err]]
   ;; TODO:
   (l/error err)
   (db/set-feed-loading db false)))

(reg-event-fx
 :activity.feed.item/dismiss
 [(path db/path)]
 (fn [{db :db} [_ dismiss-id]]
   {:db         (db/remove-feed-item db dismiss-id)
    :http-xhrio {:uri             (api/route (str "/news/" dismiss-id))
                 :method          :post
                 :params          {}
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:activity.feed.item.dismiss/success]
                 :on-failure      [:activity.feed.item.dismiss/failure (db/feed db)]}}))

(reg-event-db
 :activity.feed.item.dismiss/success
 [(path db/path)]
 (fn [db _]
   ;; no-op
   db))

(reg-event-db
 :activity.feed.item.dismiss/failure
 [(path db/path)]
 (fn [db [_ items-before err]]
   (l/error err)
   (db/set-feed db items-before)))
