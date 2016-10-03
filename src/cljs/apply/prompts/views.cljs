(ns apply.prompts.views
  (:require [apply.routes :refer [prompt-uri]]
            [apply.prompts.models :as prompts]
            [starcity.components.icons :as i]
            [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as r]
            [starcity.dom :as dom])
  (:refer-clojure :exclude [next]))

;; =============================================================================
;; Internal Components
;; =============================================================================

(defn- next-button [curr-prompt complete]
  (let [loading (subscribe [:prompt/is-loading])
        label   (subscribe [:prompt/next-button-label])]
    (fn [curr-prompt complete]
      [:button.next.is-medium.button.is-primary
       {:type  "submit"
        :class (str (when-not (prompts/locally-complete? complete)
                      "is-disabled")
                    (when @loading " is-loading"))}
       [:span @label]
       (i/angle-right)])))

(defn- save-button [curr-prompt complete]
  (let [saving   (subscribe [:prompt/is-saving])
        can-save (subscribe [:prompt/can-save?])]
    (fn [curr-prompt complete]
      [:button.is-medium.button.is-info
       {:type     "button"
        :on-click #(dispatch [:prompt/save])
        :class    (str (when @saving "is-loading")
                       (when-not @can-save " is-disabled"))}
       [:span (if @can-save "Save" "Saved")]])))

(defn- previous-button [previous-prompt]
  [:a.button.is-medium.previous {:href (prompt-uri previous-prompt)}
   (i/angle-left)
   [:span "Back"]])

(defn- footer []
  (let [curr-prompt     (subscribe [:prompt/current])
        complete        (subscribe [(prompts/complete-key @curr-prompt)])
        previous-prompt (subscribe [:prompt/previous])]
    (fn []
      [:div.columns.is-mobile.prompt-controls
       (when @previous-prompt
         [:div.column.has-text-left [previous-button @previous-prompt]])
       [:div.column
        [:div.is-grouped.control
         {:style {:justify-content "flex-end"}}
         (when (prompts/complete? @complete)
           [:p.control [save-button @curr-prompt @complete]])
         [:p.control [next-button @curr-prompt @complete]]]]])))

(def ^:private mo-image
  [:img.is-circular
   {:src "/assets/img/mo.jpg" :alt "community advisor headshot"}])

(defn- contact-modal []
  (let [question (subscribe [:prompt.help/form-data])
        showing  (subscribe [:prompt.help/showing?])
        can-send (subscribe [:prompt.help/can-send?])
        loading  (subscribe [:prompt.help/loading?])]
    (fn []
      [:div.modal {:class (when @showing "is-active")}
       [:div.modal-background
        {:on-click #(dispatch [:prompt.help/toggle])}]
       [:div.modal-content
        [:div.box
         [:div.media
          [:figure.media-left
           [:p.image.is-96x96 mo-image]]
          [:div.media-content
           [:form {:on-submit #(do (.preventDefault %)
                                   (dispatch [:prompt.help/send]))}
            [:p.title.is-5 "How can I help?"]
            [:p.control
             [:textarea.textarea
              {:placeholder "Enter your question here."
               :value       @question
               :on-change   #(dispatch [:prompt.help/change (dom/val %)])}]]
            [:p.control
             [:button.button.is-info
              {:type  "submit"
               :class (str (when-not @can-send "is-disabled")
                           (when @loading " is-loading"))}
              "Send"]]]]]]]
       [:button.modal-close {:on-click #(dispatch [:prompt.help/toggle])}]])))

;; =============================================================================
;; API
;; =============================================================================

(defn header
  [title]
  [:header
   [:figure.image.is-64x64
    [:a {:on-click #(dispatch [:prompt.help/toggle])} mo-image]]
   [:h3.prompt-title.title.is-4 title]
   [contact-modal]])

(defn content [content]
  [:div.prompt-content
   content])

(defn prompt [header content]
  [:div.prompt
   header
   [:form {:on-submit #(do
                         (.preventDefault %)
                         (dispatch [:prompt/next]))}
    content
    [footer]]])
