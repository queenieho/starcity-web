(ns starcity.api.admin.notes
  (:require [blueprints.models.events :as events]
            [blueprints.models.note :as note]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [clojure.spec :as s]
            [compojure.core :refer [defroutes DELETE GET POST PUT]]
            [datomic.api :as d]
            [starcity.auth :as auth]
            [starcity.datomic :refer [conn]]
            [starcity.util.response :as response]
            [starcity.util.validation :as uv]
            [toolbelt.core :as tb]
            [toolbelt.predicates :as p]))

;; =============================================================================
;; Handlers
;; =============================================================================

(defn- is-author? [account note]
  (= (:db/id account) (:db/id (note/author note))))

;; =============================================================================
;; Comments

(defn create-comment!
  "Create a comment under `note-id`."
  [conn note-id author {:keys [content notify]}]
  (let [note    (d/entity (d/db conn) note-id)
        comment (note/create-comment author content)]
    @(d/transact conn (tb/conj-when
                       [(note/add-comment note comment)]
                       (when notify (events/note-comment-created note comment))))
    {:result "ok"}))

(s/def ::content string?)
(s/def ::notify boolean?)
(s/fdef create-comment!
        :args (s/cat :conn p/conn?
                     :note-id integer?
                     :author p/entity?
                     :params (s/keys :req-un [::content] :opt-un [::notify]))
        :ret (s/keys :req-un [::result]))

;; =============================================================================
;; Delete

(defn delete-note!
  "Delete the note with id `note-id`."
  [conn account note-id]
  (let [note (d/entity (d/db conn) note-id)]
   (if (is-author? account note)
     (do
       @(d/transact conn [[:db.fn/retractEntity note-id]])
       (response/transit-ok {:result "ok"}))
     (response/transit-forbidden {:message "You did not author this note, so you cannot delete it."}))))

;; =============================================================================
;; Convert

(defn convert-note!
  "Convert note with id `note-id` into a ticket. Only supports going from note
  to ticket at this time."
  [conn note-id]
  (let [note (d/entity (d/db conn) note-id)]
    (if (note/ticket? note)
      (response/transit-unprocessable {:message "This note is already a ticket."})
      (do
        @(d/transact conn [{:db/id         (:db/id note)
                            :ticket/status :ticket.status/open}])
        (response/transit-ok {:result "ok"})))))

;; =============================================================================
;; Status

(defn change-status!
  "Toggle ticket's status from open to closed and vice-versa. Requires that the
  note identified by `note-id` be a ticket."
  [conn note-id]
  (let [note (d/entity (d/db conn) note-id)]
    (if-not (note/ticket? note)
      (response/transit-unprocessable
       {:message "Note must be a ticket before it can be opened/closed!"})
      (do
        @(d/transact conn [(note/toggle-status note)])
        (response/transit-ok {:result "ok"})))))

;; =============================================================================
;; Update

(defn update-note!
  [conn account note-id params]
  (let [note    (d/entity (d/db conn) note-id)
        vresult (b/validate params {:content [v/required v/string]
                                    :subject [v/required v/string]})]
    (cond
      (not (is-author? account note))
      (response/transit-forbidden
       {:message "You are not the author of this note."})

      (not (uv/valid? vresult))
      (response/transit-malformed
       {:message (first (uv/errors vresult))})

      :otherwise (let [{:keys [subject content]} (uv/result vresult)]
                   @(d/transact conn [(note/update note
                                                   :subject subject
                                                   :content content)])
                   (response/transit-ok {:result "ok"})))))

;; =============================================================================
;; Routes
;; =============================================================================

(defroutes routes

  (GET "/:note-id" [note-id]
       (fn [_]
         (let [db   (d/db conn)
               note (d/entity db (tb/str->int note-id))]
           (response/transit-ok
            {:result (note/clientize db note)}))))

  (PUT "/:note-id" [note-id]
       (fn [{params :params :as req}]
         (let [requester (auth/requester req)]
           (update-note! conn requester (tb/str->int note-id) params))))

  (DELETE "/:note-id" [note-id]
          (fn [req]
            (delete-note! conn (auth/requester req) (tb/str->int note-id))))

  (POST "/:note-id/convert" [note-id]
        (fn [_]
          (convert-note! conn (tb/str->int note-id))))

  (POST "/:note-id/status" [note-id]
        (fn [_]
          (change-status! conn (tb/str->int note-id))))

  (POST "/:note-id/comments" [note-id]
        (fn [{params :params :as req}]
          (let [vresult (b/validate params {:content [v/required v/string]
                                            :notify  v/boolean})
                author  (auth/requester req)]
            (if-let [params (uv/valid? vresult)]
              (response/transit-ok
               (create-comment! conn (tb/str->int note-id) author params))
              (response/transit-malformed {:message (first (uv/errors vresult))}))))))
