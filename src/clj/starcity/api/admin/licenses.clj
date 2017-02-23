(ns starcity.api.admin.licenses
  (:require [compojure.core :refer [defroutes GET]]
            [starcity.datomic :refer [conn]]
            [starcity.models.license :as license]
            [starcity.util.response :as response]))

;; =============================================================================
;; Handlers
;; =============================================================================

(defn fetch-licenses [conn]
  {:result (->> (license/licenses conn)
                (map #(select-keys % [:db/id :license/term]))
                (sort-by :license/term))})

;; =============================================================================
;; Routes
;; =============================================================================

(defroutes routes
  (GET "/" []
       (fn [_]
         (response/transit-ok
          (fetch-licenses conn)))))
