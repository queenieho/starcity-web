(ns admin.accounts.views.entry.notes
  (:require [admin.notes.views :as note]
            [re-frame.core :refer [subscribe dispatch]]
            [toolbelt.core :as tb]
            [ant-ui.core :as a]
            [reagent.core :as r]))

;; =============================================================================
;; Controls

(defn controls []
  [a/card
   {:style {:text-align "center"}}
   [a/button
    {:type     :primary
     :size     :large
     :icon     "plus"
     :on-click #(dispatch [:note.add/show])}
    "New Note"]])

;; =============================================================================
;; New Note

(defn- new-note-footer []
  (let [can-submit (subscribe [:note.add/can-submit?])
        submitting (subscribe [:note.add/submitting?])
        viewing    (subscribe [:accounts/viewing])]
    [:div
     [a/button {:size     :large
                :on-click #(dispatch [:note.add/hide])}
      "Cancel"]
     [a/button {:size     :large
                :type     :primary
                :disabled (not @can-submit)
                :loading  @submitting
                :on-click #(dispatch [:note.add/submit! @viewing])}
      "Create"]]))

(defn- new-note-form []
  (let [visible   (subscribe [:note.add/visible?])
        form-data (subscribe [:note.add/form-data])]
    [a/modal {:title     "Add Note"
              :visible   @visible
              :footer    (r/as-element [new-note-footer])
              :on-cancel #(dispatch [:note.add/hide])}
     [:div
      [:div.control.is-expanded
       [:label.label "Subject"]
       [a/input
        {:type      "text"
         :value     (:subject @form-data)
         :on-change #(dispatch [:note.add/update :subject (.. % -target -value)])}]]

      [:div.control.is-expanded
       [:label.label "Content"]
       [a/input
        {:type      "textarea"
         :rows      3
         :value     (:content @form-data)
         :on-change #(dispatch [:note.add/update :content (.. % -target -value)])}]]

      [:div.control.is-grouped
       [:div.control
        [a/checkbox
         {:checked   (:ticket @form-data)
          :on-change #(dispatch [:note.add/update :ticket (.. % -target -checked)])}
         "This note is a ticket"]
        [a/checkbox
         {:checked (:notify @form-data)
          :on-change #(dispatch [:note.add/update :notify (.. % -target -checked)])}
         "Send Slack notification upon creation"]]]]]))

;; =============================================================================
;; Edit Note

(defn- edit-note-footer []
  (let [can-submit (subscribe [:note.edit/can-submit?])
        submitting (subscribe [:note.edit/submitting?])
        viewing    (subscribe [:accounts/viewing])]
    [:div
     [a/button {:size     :large
                :on-click #(dispatch [:note.edit/hide])}
      "Cancel"]
     [a/button {:size     :large
                :type     :primary
                :disabled (not @can-submit)
                :loading  @submitting
                :on-click #(dispatch [:note.edit/submit!])}
      "Update"]]))

(defn- edit-note-form []
  (let [visible   (subscribe [:note.edit/visible?])
        form-data (subscribe [:note.edit/form-data])]
    [a/modal {:title     "Edit Note"
              :visible   @visible
              :footer    (r/as-element [edit-note-footer])
              :on-cancel #(dispatch [:note.edit/hide])}
     [:div
      [:div.control.is-expanded
       [:label.label "Subject"]
       [a/input
        {:type      "text"
         :value     (:subject @form-data)
         :on-change #(dispatch [:note.edit/update :subject (.. % -target -value)])}]]

      [:div.control.is-expanded
       [:label.label "Content"]
       [a/input
        {:type      "textarea"
         :rows      3
         :value     (:content @form-data)
         :on-change #(dispatch [:note.edit/update :content (.. % -target -value)])}]]]]))

;; =============================================================================
;; Entrypoint
;; =============================================================================

(defn notes []
  (let [viewing    (subscribe [:accounts/viewing])
        is-loading (subscribe [:notes/fetching?])
        notes      (subscribe [:notes])]
    (fn []
      (let [notes (get @notes @viewing)]
        [:div.columns
         [:div.column.is-two-thirds
          (cond
            @is-loading    [a/card {:loading true}]
            (empty? notes) [a/card [:p "No notes."]]

            :otherwise
            (map-indexed
             #(with-meta [:div {:style {:margin-bottom 8}} [note/note %2]] {:key %1})
             notes))]
         [:div.column
          [edit-note-form]
          [new-note-form]
          [controls]]]))))
