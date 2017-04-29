(ns starcity.util.request
  (:require [datomic.api :as d]
            [clojure.spec :as s]
            [toolbelt.predicates :as p]))

(defn requester
  "Produce the `account` entity that initiated this `request`."
  [db request]
  (let [id (get-in request [:identity :db/id])]
    (d/entity db id)))

(s/fdef requester
        :args (s/cat :db p/db? :request map?)
        :ret (s/or :nothing nil? :entity p/entity?))
