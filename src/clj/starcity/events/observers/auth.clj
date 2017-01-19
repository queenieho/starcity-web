(ns starcity.events.observers.auth
  (:require [clojure.core.async :refer [chan sliding-buffer]]
            [datomic.api :as d]
            [starcity.datomic :refer [conn]]
            [starcity.events.plumbing :refer [defobserver]]
            [taoensso.timbre :as timbre]))

(defn- deauthorize [account]
  (when-let [session-id (d/q '[:find ?e .
                               :in $ ?a
                               :where
                               [?e :session/account ?a]]
                             (d/db conn) (:db/id account))]
    @(d/transact conn [[:db.fn/retractEntity session-id]])))

(defmulti handler :event)

(defmethod handler :starcity.events.promote/to-member
  [{:keys [account] :as ev}]
  (timbre/info ::deauthorize {:account (:db/id account)})
  (deauthorize account))

(defmethod handler :default
  [{event :event}]
  (timbre/debug "unhandled event" event))

(defobserver auth (chan (sliding-buffer 4096)) handler)
