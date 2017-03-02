(ns admin.notes.events
  (:require [admin.notes.db :as db]
            [day8.re-frame.http-fx]
            [re-frame.core :refer [reg-event-db
                                   reg-event-fx
                                   path]]
            [ajax.core :as ajax]
            [toolbelt.core :as tb]
            [plumbing.core :as plumbing]))

;; =============================================================================
;; Fetch Note
;; =============================================================================

(reg-event-fx
 :note/fetch
 [(path db/path)]
 (fn [_ [_ note-id]]
   {:alert/message {:type :loading :duration :indefinite :content "Loading..."}
    :http-xhrio    {:method          :get
                    :uri             (str "/api/v1/admin/notes/" note-id)
                    :response-format (ajax/transit-response-format)
                    :on-success      [:note.fetch/success]
                    :on-failure      [:note.fetch/failure]}}))

(reg-event-fx
 :note.fetch/success
 [(path db/path)]
 (fn [{:keys [db]} [_ {note :result}]]
   {:db                 (db/update-note db note)
    :alert.message/hide true}))

(reg-event-fx
 :note.fetch/failure
 [(path db/path)]
 (fn [{:keys [db]} [_ {res :response :as error}]]
   (tb/error error)
   {:alert/message {:type    :error
                    :content "Failed to refresh note!"}}))

;; =============================================================================
;; Fetch Account Notes
;; =============================================================================

(reg-event-fx
 :notes.account/fetch
 [(path db/path)]
 (fn [{:keys [db]} [_ account-id]]
   {:db         (db/is-fetching-notes db)
    :http-xhrio {:method          :get
                 :uri             (str "/api/v1/admin/accounts/" account-id "/notes")
                 :response-format (ajax/transit-response-format)
                 :on-success      [:notes.account.fetch/success account-id]
                 :on-failure      [:notes.account.fetch/failure]}}))

(reg-event-fx
 :notes.account.fetch/success
 [(path db/path)]
 (fn [{:keys [db]} [_ account-id {result :result}]]
   {:db (db/done-fetching-notes db account-id result)}))

(reg-event-fx
 :notes.account.fetch/failure
 [(path db/path)]
 (fn [{:keys [db]} [_ err]]
   (tb/error err)
   {:db            (db/error-fetching-notes db err)
    :alert/message {:type    :error
                    :content "Error encountered while trying to fetch notes!"}}))

;; =============================================================================
;; Add Note
;; =============================================================================

(reg-event-db
 :note.add/show
 [(path db/path)]
 (fn [db _]
   (assoc-in db [:add-note :visible] true)))

(reg-event-db
 :note.add/hide
 [(path db/path)]
 (fn [db _]
   (assoc-in db [:add-note :visible] false)))

(reg-event-db
 :note.add/update
 [(path db/path)]
 (fn [db [_ k v]]
   (assoc-in db [:add-note :form-data k] v)))

(reg-event-fx
 :note.add/submit!
 [(path db/path)]
 (fn [{:keys [db]} [_ account-id]]
   (let [{:keys [subject content notify ticket]} (get-in db [:add-note :form-data])]
     {:db         (assoc-in db [:add-note :submitting] true)
      :http-xhrio {:method          :post
                   :uri             (str "/api/v1/admin/accounts/" account-id "/notes")
                   :params          {:subject subject
                                     :content content
                                     :notify  (boolean notify)
                                     :ticket  (boolean ticket)}
                   :format          (ajax/transit-request-format)
                   :response-format (ajax/transit-response-format)
                   :on-success      [:note.add.submit/success account-id]
                   :on-failure      [:note.add.submit/failure]}})))

(reg-event-fx
 :note.add.submit/success
 [(path db/path)]
 (fn [{:keys [db]} [_ account-id]]
   {:db         (-> (assoc-in db [:add-note :submitting] false)
                    (plumbing/dissoc-in [:add-note :form-data]))
    :dispatch-n [[:notes.account/fetch account-id]
                 [:note.add/hide]]}))

(reg-event-fx
 :note.add.submit/failure
 [(path db/path)]
 (fn [{:keys [db]} [_ {res :response :as error}]]
   (tb/error error)
   {:db           (assoc-in db [:add-note :submitting] false)
    :alert/notify {:type     :error
                   :duration 6.0
                   :title    "Error encountered!"
                   :content  (or (:message res)
                                 "An unknown error has occurred.")}}))

;; =============================================================================
;; Edit Note
;; =============================================================================

(reg-event-db
 :note.edit/show
 [(path db/path)]
 (fn [db [_ note]]
   (-> (assoc-in db [:edit-note :visible] true)
       (assoc-in [:edit-note :form-data]
                 {:id      (:db/id note)
                  :subject (:note/subject note)
                  :content (:note/content note)}))))

(reg-event-db
 :note.edit/hide
 [(path db/path)]
 (fn [db _]
   (assoc-in db [:edit-note :visible] false)))

(reg-event-db
 :note.edit/update
 [(path db/path)]
 (fn [db [_ k v]]
   (assoc-in db [:edit-note :form-data k] v)))

(reg-event-fx
 :note.edit/submit!
 [(path db/path)]
 (fn [{:keys [db]} _]
   (let [{:keys [id subject content]} (get-in db [:edit-note :form-data])]
     {:db         (assoc-in db [:edit-note :submitting] true)
      :http-xhrio {:method          :put
                   :uri             (str "/api/v1/admin/notes/" id)
                   :params          {:subject subject
                                     :content content}
                   :format          (ajax/transit-request-format)
                   :response-format (ajax/transit-response-format)
                   :on-success      [:note.edit.submit/success id]
                   :on-failure      [:note.edit.submit/failure]}})))

(reg-event-fx
 :note.edit.submit/success
 [(path db/path)]
 (fn [{:keys [db]} [_ note-id]]
   {:db         (-> (assoc-in db [:edit-note :submitting] false)
                    (plumbing/dissoc-in [:edit-note :form-data]))
    :dispatch-n [[:note/fetch note-id]
                 [:note.edit/hide]]}))

(reg-event-fx
 :note.edit.submit/failure
 [(path db/path)]
 (fn [{:keys [db]} [_ {res :response :as error}]]
   (tb/error error)
   {:db           (assoc-in db [:edit-note :submitting] false)
    :alert/notify {:type     :error
                   :duration 6.0
                   :title    "Error encountered!"
                   :content  (or (:message res)
                                 "An unknown error has occurred.")}}))

;; =============================================================================
;; Comments
;; =============================================================================

(reg-event-db
 :note.comment/cancel
 [(path db/path)]
 (fn [db [_ note-id]]
   (plumbing/dissoc-in db [:comments note-id])))

(reg-event-db
 :note/comment
 [(path db/path)]
 (fn [db [_ note-id]]
   (assoc-in db [:comments note-id] {:content ""
                                     :notify  true})))

(reg-event-db
 :note.comment/update
 [(path db/path)]
 (fn [db [_ note-id k v]]
   (assoc-in db [:comments note-id k] v)))

(reg-event-fx
 :note.comment/create!
 [(path db/path)]
 (fn [{:keys [db]} [_ note-id]]
   (let [{:keys [content notify]} (get-in db [:comments note-id])]
     {:db         (assoc-in db [:comments note-id :submitting] true)
      :http-xhrio {:method          :post
                   :uri             (str "/api/v1/admin/notes/" note-id "/comments")
                   :params          {:content content
                                     :notify  (boolean notify)}
                   :format          (ajax/transit-request-format)
                   :response-format (ajax/transit-response-format)
                   :on-success      [:note.comment.create/success note-id]
                   :on-failure      [:note.comment.create/failure note-id]}})))

(reg-event-fx
 :note.comment.create/success
 [(path db/path)]
 (fn [{:keys [db]} [_ note-id _]]
   {:db         (assoc-in db [:comments note-id :submitting] false)
    :dispatch-n [[:note/fetch note-id]
                 [:note.comment/cancel note-id]]}))

(reg-event-fx
 :note.comment.create/failure
 [(path db/path)]
 (fn [{:keys [db]} [_ note-id {res :response :as error}]]
   (tb/error error)
   {:db           (assoc-in db [:comments note-id :submitting] false)
    :alert/notify {:type     :error
                   :duration 6.0
                   :title    "Error encountered!"
                   :content  (or (:message res)
                                 "An unknown error has occurred!")}}))

;; =============================================================================
;; Delete
;; =============================================================================

;; To show confirmation modal
(reg-event-db
 :note/delete
 [(path db/path)]
 (fn [db [_ note]]
   (assoc db :pending-deletion note)))

(reg-event-db
 :note.delete/cancel
 [(path db/path)]
 (fn [db _]
   (dissoc db :pending-deletion)))

(reg-event-fx
 :note/delete!
 [(path db/path)]
 (fn [_ [_ note]]
   {:alert/message {:type :loading :duration 10.0 :content "Deleting..."}
    :http-xhrio    {:method          :delete
                    :uri             (str "/api/v1/admin/notes/" (:db/id note))
                    :format          (ajax/transit-request-format)
                    :response-format (ajax/transit-response-format)
                    :on-success      [:note.delete/success note]
                    :on-failure      [:note.delete/failure]}}))

(reg-event-fx
 :note.delete/success
 [(path db/path)]
 (fn [{:keys [db]} [_ note]]
   {:db            (-> (dissoc db :pending-deletion)
                       (db/remove-note note))
    :alert/message {:type :success :content "Note deleted!"}}))

(reg-event-fx
 :note.delete/failure
 [(path db/path)]
 (fn [_ [_ error]]
   (tb/error error)
   {:alert/message {:type :error :content "Failed to delete note."}}))

;; =============================================================================
;; Tickets
;; =============================================================================

;; Converts a plain note into a ticket.
(reg-event-fx
 :note/convert
 [(path db/path)]
 (fn [_ [_ note]]
   {:alert/message {:type :loading :duration 10.0 :content "Converting..."}
    :http-xhrio    {:method          :post
                    :uri             (str "/api/v1/admin/notes/" (:db/id note) "/convert")
                    :format          (ajax/transit-request-format)
                    :response-format (ajax/transit-response-format)
                    :on-success      [:note.convert/success note]
                    :on-failure      [:note.convert/failure]}}))

(reg-event-fx
 :note.convert/success
 [(path db/path)]
 (fn [{:keys [db]} [_ note]]
   {:dispatch      [:note/fetch (:db/id note)]
    :alert/message {:type :success :content "Note converted!"}}))

(reg-event-fx
 :note.convert/failure
 [(path db/path)]
 (fn [_ [_ error]]
   (tb/error error)
   {:alert/message {:type :error :content "Failed to convert note."}}))

(reg-event-fx
 :note.ticket.status/toggle!
 [(path db/path)]
 (fn [_ [_ note]]
   {:alert/message {:type :loading :duration 10.0 :content "Changing status..."}
    :http-xhrio    {:method          :post
                    :uri             (str "/api/v1/admin/notes/" (:db/id note) "/status")
                    :format          (ajax/transit-request-format)
                    :response-format (ajax/transit-response-format)
                    :on-success      [:note.ticket.status.toggle/success note]
                    :on-failure      [:note.ticket.status.toggle/failure]}}))

(reg-event-fx
 :note.ticket.status.toggle/success
 [(path db/path)]
 (fn [{:keys [db]} [_ note]]
   {:dispatch      [:note/fetch (:db/id note)]
    :alert/message {:type :success :content "Status changed!"}}))

(reg-event-fx
 :note.ticket.status.toggle/failure
 [(path db/path)]
 (fn [_ [_ error]]
   (tb/error error)
   {:alert/message {:type :error :content "Failed to change status."}}))
