(ns admin.notes.subs
  (:require [admin.notes.db :as db]
            [re-frame.core :refer [reg-sub]]
            [clojure.string :as string]))

;; =============================================================================
;; Global
;; =============================================================================

(reg-sub
 ::notes
 (fn [db _]
   (db/path db)))

(reg-sub
 :notes
 :<- [::notes]
 (fn [db _]
   (db/notes db)))

;; =============================================================================
;; Fetch Notes
;; =============================================================================

(reg-sub
 :notes/fetching?
 :<- [::notes]
 (fn [db _]
   (db/fetching-notes? db)))

;; =============================================================================
;; Add Note
;; =============================================================================

(reg-sub
 :note/add
 :<- [::notes]
 (fn [db _]
   (:add-note db)))

(reg-sub
 :note.add/visible?
 :<- [:note/add]
 (fn [db _]
   (:visible db)))

(reg-sub
 :note.add/form-data
 :<- [:note/add]
 (fn [db _]
   (:form-data db)))

(reg-sub
 :note.add/can-submit?
 :<- [:note.add/form-data]
 (fn [{:keys [subject content] :as data} _]
   (not (or (string/blank? subject)
            (string/blank? content)))))

(reg-sub
 :note.add/submitting?
 :<- [:note/add]
 (fn [db _]
   (:submitting db)))

;; =============================================================================
;; Edit Note
;; =============================================================================

(reg-sub
 :note/edit
 :<- [::notes]
 (fn [db _]
   (:edit-note db)))

(reg-sub
 :note.edit/visible?
 :<- [:note/edit]
 (fn [db _]
   (:visible db)))

(reg-sub
 :note.edit/form-data
 :<- [:note/edit]
 (fn [db _]
   (:form-data db)))

(reg-sub
 :note.edit/can-submit?
 :<- [:note.edit/form-data]
 (fn [{:keys [subject content] :as data} _]
   (not (or (string/blank? subject)
            (string/blank? content)))))

(reg-sub
 :note.edit/submitting?
 :<- [:note/edit]
 (fn [db _]
   (:submitting db)))

;; =============================================================================
;; Comment
;; =============================================================================

(reg-sub
 ::comments
 :<- [::notes]
 (fn [db _]
   (:comments db)))

(reg-sub
 :note.comment/form-data
 :<- [::comments]
 (fn [db [_ note-id]]
   (get db note-id)))

(reg-sub
 :note.comment/can-submit?
 :<- [::comments]
 (fn [db [_ note-id]]
   (not (string/blank? (get-in db [note-id :content])))))

(reg-sub
 :note.comment/submitting?
 :<- [::comments]
 (fn [db [_ note-id]]
   (get-in db [note-id :submitting])))

;; =============================================================================
;; Deletion
;; =============================================================================

(reg-sub
 :note/pending-deletion
 :<- [::notes]
 (fn [db _]
   (:pending-deletion db)))
