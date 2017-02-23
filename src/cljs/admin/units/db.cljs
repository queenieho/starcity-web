(ns admin.units.db
  (:require [toolbelt.core :as tb]))

(def path ::units)
(def default-value
  {path {:loading {:unit false}
         :units   {}
         :viewing nil}})

(defn- set-loading [db k to]
  (assoc-in db [:loading k] to))

(defn viewing-unit-id [db unit-id]
  (assoc db :viewing unit-id))

(defn is-fetching-unit [db]
  (set-loading db :unit true))

(defn fetching-unit? [db]
  (get-in db [:loading :unit]))

(defn done-fetching-unit
  [db unit]
  (tb/log "done-fetching-unit" unit)
  (-> (assoc-in db [:units (:db/id unit)] unit)
      (set-loading :unit false)))

(defn error-fetching-unit
  [db error]
  ;; TODO: do something with error
  (tb/error "error-fetching-unit" error)
  (set-loading db :unit false))
