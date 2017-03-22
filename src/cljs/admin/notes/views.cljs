(ns admin.notes.views
  (:require [re-frame.core :refer [subscribe dispatch]]
            [ant-ui.core :as a]
            [toolbelt.date :as date]
            [toolbelt.core :as tb]
            [reagent.core :as r]
            [clojure.string :as string]))

;; =============================================================================
;; Misc
;; =============================================================================

(def ^:private divider
  [:span {:dangerouslySetInnerHTML {:__html "&middot;"} :style {:margin "0 4px"}}])

(defn deletion-modal []
  (let [note (subscribe [:note/pending-deletion])]
    [a/modal {:visible     (boolean @note)
              :title       "Are you sure you want to delete this note?"
              :key         (:db/id @note)
              :ok-text     "Yes"
              :cancel-text "Cancel"
              :on-cancel   #(dispatch [:note.delete/cancel])
              :on-ok       #(dispatch [:note/delete! @note])}
     [:p [:strong (:note/subject @note)]]
     [:p (:note/content @note)]]))

;; =============================================================================
;; Comments
;; =============================================================================

(defn- note-comment [note]
  [:article.media
   [:figure.media-left
    [a/icon {:type "message" :class "note__icon"}]]
   [:div.media-content
    [:div.content
     [:p [:strong (-> note :note/author :account/name)]]
     [:p (:note/content note)]
     [:small {:style {:font-size "10px"}}
      (date/short-date-time (:note/created-at note))]]]])

(defn- edit-comment [note-id {:keys [content notify]}]
  (let [can-submit (subscribe [:note.comment/can-submit? note-id])
        submitting (subscribe [:note.comment/submitting? note-id])]
    [:article.media
     [:figure.media-left
      [a/icon {:type "message" :class "note__icon"}]]
     [:div.media-content
      [:p.control
       [a/input
        {:type        "textarea"
         :placeholder "Add your comment here..."
         :on-change   #(dispatch [:note.comment/update note-id :content (.. % -target -value)])
         :value       content}]]

      [:p.control
       [a/checkbox
        {:checked   notify
         :on-change #(dispatch [:note.comment/update note-id :notify (.. % -target -checked)])}
        "Send Slack notifications to subscribers"]]

      [:p.control
       [a/button
        {:type     "ghost"
         :on-click #(dispatch [:note.comment/cancel note-id])
         :style    {:margin-right 8}}
        "Cancel"]
       [a/button
        {:type     "primary"
         :disabled (not @can-submit)
         :loading  @submitting
         :on-click #(dispatch [:note.comment/create! note-id])}
        "Comment"]]]]))

;; =============================================================================
;; Controls
;; =============================================================================

(defn- controls [showing-comments note]
  [:small.note__controls
   [:span.note__control
    (date/short-date-time (:note/created-at note))]
   [:span.note__control
    divider
    [:a
     {:on-click #(do
                   (reset! showing-comments true)
                   (dispatch [:note/comment (:db/id note)]))}
     "Comment"]]
   (when-not (-> note :note/children empty?)
     [:span.note__control
      divider
      [:a {:on-click #(swap! showing-comments not)}
       [a/icon {:type (if @showing-comments "up" "down")}]
       (str " comments (" (count (:note/children note)) ") ")]])])

;; =============================================================================
;; Actions
;; =============================================================================

(def owner-actions
  [["Edit" :note.edit/show]
   ["Delete" :note/delete]])

(defn ticket-actions [ticket]
  (if (= (:ticket/status ticket) :ticket.status/open)
    ["Close" :note.ticket.status/toggle!]
    ["Open" :note.ticket.status/toggle!]))

(def note-actions
  [["Convert to Ticket" :note/convert]])

(defn- menu-item [note [label event]]
  [a/menu-item {:class "ant-dropdown-menu-item"}
   [:a {:on-click #(dispatch [event note])} label]])

(defn- actions-for [user-id note]
  (let [is-owner  (= user-id (-> note :note/author :db/id))
        is-ticket (contains? note :ticket/status)]
    (cond-> []
      is-owner        (concat owner-actions)
      is-ticket       (conj (ticket-actions note))
      (not is-ticket) (concat note-actions))))

(defn- actions-menu [note]
  (let [auth (subscribe [:auth])]
    (fn [note]
      [a/menu {:class "ant-dropdown-menu"
               :style {:box-shadow "0 1px 6px rgba(0,0,0,0.2)"}}
       (map-indexed
        #(with-meta (menu-item note %2) {:key %1})
        (actions-for (:db/id @auth) note))])))

(defn- actions [note]
  [a/dropdown {:overlay (r/as-element [actions-menu note])}
   [:a {:class "ant-dropdown-link"} [a/icon {:type "ellipsis"}]]])

;; =============================================================================
;; Note View
;; =============================================================================

(defn- note-icon [note]
  (let [ticket? (contains? note :ticket/status)
        closed? (= (:ticket/status note) :ticket.status/closed)]
    (cond
      closed?    [a/icon {:type "check-circle" :class "note__icon"}]
      ticket?    [a/icon {:type "clock-circle-o" :class "note__icon"}]
      :otherwise [a/icon {:type "solution" :class "note__icon"}])))

(defn- subject [note]
  (let [prefix (when-let [status (:ticket/status note)] (name status))]
    (if prefix
      (str "[" (string/upper-case prefix) "]: " (:note/subject note))
      (:note/subject note))))

(defn note [note]
  (let [showing-comments (r/atom false)
        editing-comment  (subscribe [:note.comment/form-data (:db/id note)])]
    (fn [note]
      [a/card {:title (subject note)
               :extra (r/as-element [actions note])}
       [deletion-modal note]
       [:article.media.note
        [:figure.media-left
         (note-icon note)]
        [:div.media-content
         [:div.content
          (when-let [author (:note/author note)]
            [:p [:strong (:account/name note)]])
          [:p (:note/content note)]
          [controls showing-comments note]]
         (when-let [comments (and @showing-comments (:note/children note))]
           (map-indexed #(with-meta (note-comment %2) {:key %1}) comments))
         (when-let [comment @editing-comment]
           [edit-comment (:db/id note) comment])]]])))
