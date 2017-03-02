(ns admin.notes.db
  (:require [cljs.spec :as s]))

;; NOTE: Notes are stored in the db in an unusual way. Because notes are always
;; associated with some other entity, the map of `:notes` in this section of the
;; db is a map of `entity-id` to a vector of its notes, where `entity-id` is the
;; id of some non-note id.

(def path ::notes)
(def default-value
  {path {:notes    {}
         :comments {} ; map of {<note-id> <wip comment>}
         :loading  {:fetching false}

         :add-note {:visible    false
                    :submitting false
                    :form-data  {}}

         :edit-note {:visible    false
                     :submitting false
                     :form-data  {}}}})

(defn notes [db]
  (:notes db))

(s/fdef notes
        :args (s/cat :db map?)
        :ret map?)

(defn fetching-notes? [db]
  (get-in db [:loading :fetching]))

(defn is-fetching-notes [db]
  (assoc-in db [:loading :fetching] true))

(defn done-fetching-notes [db entity-id notes]
  (-> (assoc-in db [:notes entity-id] notes)
      (assoc-in [:loading :fetching] false)))

(defn error-fetching-notes [db error]
  (assoc-in db [:loading :fetching] false))

(defn update-note
  "Presumably `note` is an updated version of a note that is already stored in
  `db` Using the `:note/referenced-by` field, find the note and replace it with
  this the new `note`."
  [db note]
  (assert (contains? note :note/referenced-by))
  (update-in db [:notes (:note/referenced-by note)]
             (fn [notes]
               (map
                (fn [n]
                  (if (= (:db/id note) (:db/id n))
                    note
                    n))
                notes))))

(defn remove-note
  [db note]
  (assert (contains? note :note/referenced-by))
  (update-in db [:notes (:note/referenced-by note)]
             (fn [notes]
               (remove
                (fn [n]
                  (if (= (:db/id note) (:db/id n))
                    note
                    n))
                notes))))
