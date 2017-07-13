(ns starcity.api.mars.news
  (:require [blueprints.models.news :as news]
            [compojure.core :refer [defroutes GET POST]]
            [datomic.api :as d]
            [starcity.datomic :refer [conn]]
            [starcity.util.request :as request]
            [starcity.util.response :as res]
            [toolbelt.core :as tb]
            [toolbelt.datomic :as td]))

(defn- clientize-news-item [news]
  (tb/assoc-when
   {:id      (:db/id news)
    :content (:news/content news)}
   :action (:news/action news)
   :avatar-url (get-in news [:news/avatar :avatar/url])
   :title (:news/title news)))

(defn- query-news [db account & {:keys [limit] :or {limit 10}}]
  (->> (d/q '[:find ?e ?tx-time
              :in $ ?a
              :where
              [?e :news/account ?a ?t]
              [?e :news/dismissed false]
              [?t :db/txInstant ?tx-time]]
            db (td/id account))
       (sort-by second)
       (reverse)
       (map (comp (partial d/entity db) first))))

(defn fetch-news
  "Retrieve requester's news."
  [req]
  (let [account (request/requester (d/db conn) req)]
    (res/json-ok {:news (->> (query-news (d/db conn) account)
                             (map clientize-news-item))})))

(defn dismiss-news
  "Dismiss news item identified by `news-id`."
  [news-id]
  (fn [req]
    (let [news (d/entity (d/db conn) (tb/str->int news-id))]
      @(d/transact conn [(news/dismiss news)])
      (res/json-ok {:result "ok"}))))

(defroutes routes
  (GET "/" [] fetch-news)
  (POST "/:news-id" [news-id] (dismiss-news news-id)))
