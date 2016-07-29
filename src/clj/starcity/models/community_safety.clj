(ns starcity.models.community-safety
  (:require [starcity.models.util :refer :all]
            [starcity.datomic :refer [conn]]
            [starcity.config :refer [datomic] :rename {datomic config}]
            [datomic.api :as d]))

;; =============================================================================
;; Helpers
;; =============================================================================

(def ^:private mapify
  (partial ks->nsks :community-safety))

;; =============================================================================
;; API
;; =============================================================================

(defn create
  [account-id report-url wants-report?]
  (let [ent (mapify {:account       account-id
                     :report-url    report-url
                     :wants-report? wants-report?})
        tid (d/tempid (:partition config))
        tx  @(d/transact conn [(assoc ent :db/id tid)])]
    (d/resolve-tempid (d/db conn) (:tempids tx) tid)))
