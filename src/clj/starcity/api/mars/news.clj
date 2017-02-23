(ns starcity.api.mars.news
  (:require [compojure.core :refer [defroutes GET POST]]
            [datomic.api :as d]
            [plumbing.core :refer [assoc-when]]
            [starcity
             [auth :as auth]
             [datomic :refer [conn]]
             [util :refer :all]]
            [starcity.api.common :refer :all]
            [starcity.models.news :as news]))

(defn- clientize-news-item [news]
  (assoc-when
   {:id      (:db/id news)
    :content (:news/content news)}
   :action (:news/action news)
   :avatar-url (get-in news [:news/avatar :avatar/url])
   :title (:news/title news)))

(defn- query-news [conn account & {:keys [limit] :or {limit 10}}]
  (->> (d/q '[:find ?e ?tx-time
              :in $ ?a
              :where
              [?e :news/account ?a ?t]
              [?e :news/dismissed false]
              [?t :db/txInstant ?tx-time]]
            (d/db conn) (:db/id account))
       (sort-by second)
       (reverse)
       (map (comp (partial d/entity (d/db conn)) first))))

(defn fetch-news
  "Retrieve requester's news."
  [req]
  (let [account (auth/requester req)]
    (ok {:news (->> (query-news conn account)
                    (map clientize-news-item))})))

(defn dismiss-news
  "Dismiss news item identified by `news-id`."
  [news-id]
  (fn [req]
    (let [news (d/entity (d/db conn) (str->int news-id))]
      @(d/transact conn [(news/dismiss news)])
      (ok {:result "ok"}))))

(defroutes routes
  (GET "/" [] fetch-news)
  (POST "/:news-id" [news-id] (dismiss-news news-id)))
