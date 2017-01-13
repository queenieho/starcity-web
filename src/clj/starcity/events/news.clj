(ns starcity.events.news
  (:require [clojure.core.async :refer [go]]
            [clojure.spec :as s]
            [datomic.api :as d]
            [dire.core :refer [with-pre-hook!]]
            [starcity
             [datomic :refer [conn]]
             [util :refer [entity?]]]
            [starcity.events.util :refer :all]
            [starcity.models.news :as news]
            [taoensso.timbre :as timbre]))

(defn dismiss!
  "Dismiss a news item."
  [news]
  (go
    (try
      @(d/transact conn [(news/dismiss news)])
      (catch Throwable ex
        (timbre/error ex ::dismiss {:news (:db/id news)})
        ex))))

(with-pre-hook! #'dismiss!
  (fn [news]
    (timbre/info ::dismiss {:news (:db/id news)})))

(s/fdef dismiss!
        :args (s/cat :news entity?)
        :ret chan?)
