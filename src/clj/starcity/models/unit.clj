(ns starcity.models.unit
  (:require [datomic.api :as d]
            [clojure.spec :as s]
            [toolbelt.core :refer [find-by]]
            [toolbelt.predicates :as p]
            [clj-time.coerce :as c]
            [clj-time.core :as t]))

;; =============================================================================
;; Selectors

(def property :property/_units)

(defn rate
  "Determine the rate for `unit` given `license`.

  Produces the unit's rate if defined; otherwise falls back to the property's
  rate."
  [unit license]
  (let [ulps (:unit/licenses unit)
        plps (-> unit property :property/licenses)
        pred #(= (:db/id license) (-> % :license-price/license :db/id))]
    (-> (or
         (find-by pred ulps)
         (find-by pred plps))
        :license-price/price)))

(s/fdef rate
        :args (s/cat :unit p/entity? :license p/entity?)
        :ret float?)

;; =============================================================================
;; Predicates

(defn available?
  "Returns true iff `unit` is not or will not be occupied by someone during
  `date`. `date` defaults to /now/ if not supplied."
  ([db unit]
   (available? db unit (java.util.Date.)))
  ([db unit date]
   (empty?
    (d/q '[:find ?ml
           :in $ ?u ?date
           :where
           ;; all mls that reference unit u
           [?ml :member-license/unit ?u]
           ;; any active or renewal licenses...
           (or [?ml :member-license/status :member-license.status/active]
               [?ml :member-license/status :member-license.status/renewal])
           ;; ...that are active during ?date
           [(.before ^java.util.Date ?date ?ends)]
           [?ml :member-license/ends ?ends]]
         db (:db/id unit) date))))

(s/fdef available?
        :args (s/cat :db p/db? :unit p/entity? :date (s/? inst?))
        :ret boolean?)

;; =====================================
;; Queries

(defn occupied-by
  "Produces the account entity of the member who lives in `unit`."
  [conn unit]
  (->> (d/q '[:find ?a .
              :in $ ?u
              :where
              [?a :account/licenses ?l]
              [?l :member-license/unit ?u]
              [?l :member-license/status :member-license.status/active]]
            (d/db conn) (:db/id unit))
       (d/entity (d/db conn))))

(s/fdef occupied-by
        :args (s/cat :conn p/conn? :unit p/entity?)
        :ret p/entity?)

(defn by-name
  "Look up a unit by `:unit/name`."
  [db unit-name]
  (d/entity db [:unit/name unit-name]))

(s/fdef by-name
        :args (s/cat :db p/db? :name string?)
        :ret p/entity?)
