(ns starcity.api.admin.licenses
  (:require [compojure.core :refer [defroutes GET]]
            [datomic.api :as d]
            [starcity.datomic :refer [conn]]
            [starcity.util.response :as response]))


;; =============================================================================
;; Handlers
;; =============================================================================


(defn fetch-licenses [db]
  (let [licenses (d/q '[:find [?e ...]
                        :where
                        [?e :license/term _]]
                      db)]
    {:result (->> (map (partial d/entity db) licenses)
                  (map #(select-keys % [:db/id :license/term]))
                  (sort-by :license/term))}))


;; =============================================================================
;; Routes
;; =============================================================================


(defroutes routes
  (GET "/" [] (fn [_] (response/transit-ok (fetch-licenses (d/db conn))))))
