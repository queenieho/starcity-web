(ns admin.properties.db
  (:require [toolbelt.core :as tb]))

(def path ::properties)
(def default-value
  {path {:loading           {:overview false
                             :property false
                             :units    false}
         :viewing           nil
         :overview-ordering [] ; list of property ids
         :properties        {} ; map of property id to property data
         }})

(defn- set-loading [db k to]
  (assoc-in db [:loading k] to))

(defn properties [db]
  (:properties db))

;; =============================================================================
;; Overview

(defn is-fetching-overview [db]
  (set-loading db :overview true))

(defn done-fetching-overview [db properties]
  (let [ids (map :db/id properties)]
    (-> (assoc db
               :overview-ordering ids
               :properties (reduce
                            #(update %1 (:db/id %2) merge %2)
                            (:properties db)
                            properties))
        (set-loading :overview false))))

(defn error-fetching-overview [db error]
  ;; TODO:
  (set-loading db :overview false))

(defn fetching-overview? [db]
  (get-in db [:loading :overview]))

;; =============================================================================
;; Entry

(defn viewing-property-id [db property-id]
  (assoc db :viewing property-id))

(defn is-fetching-property [db]
  (set-loading db :property true))

(defn fetching-property? [db]
  (get-in db [:loading :property]))

(defn done-fetching-property
  "Merge the server-side information about `property` with the current
  information we have about `property`."
  [db property]
  (-> (update-in db [:properties (:db/id property)] merge property)
      (set-loading :property false)))

(defn error-fetching-property
  [db error]
  ;; TODO: do something with error
  (set-loading db :property false))

(defn is-updating-property [db]
  (set-loading db :update true))

(defn done-updating-property [db]
  (set-loading db :update false))

(defn error-updating-property [db error]
  ;; TODO:
  (set-loading db :update false))
