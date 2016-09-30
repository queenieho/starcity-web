(ns apply.prompts.views
  (:require [apply.routes :refer [prompt-uri]]
            [apply.prompts.models :as prompts]
            [starcity.components.icons :as i]
            [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as r])
  (:refer-clojure :exclude [next]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- next-button [label]
  (let [curr-prompt (subscribe [:prompt/current])
        loading?    (subscribe [:prompt/loading])
        complete?   (subscribe [(prompts/complete-key @curr-prompt) :unsynced])]
    (fn [label]
      [:a.next.is-medium.button.is-primary.is-pulled-right
       {:on-click #(dispatch [:prompt/next])
        :class    (str (when-not @complete? "is-disabled ")
                       (when @loading? " is-loading"))}
       [:span label]
       (i/angle-right)])))

(defn- next-link*
  [link-to label]
  [:a.next.is-medium.button.is-primary.is-pulled-right
   {:href (prompt-uri link-to)}
   [:span label]
   (i/angle-right)])

(defn- previous-button []
  (let [previous-prompt (subscribe [:prompt/previous])]
    (fn []
      [:a.button.is-medium.previous {:href (prompt-uri @previous-prompt)}
       (i/angle-left)
       [:span "Previous"]])))

;; =============================================================================
;; API
;; =============================================================================

(defn header
  [title & [subtitle]]
  [:header
   [:h3.title.is-4 title]
   (when subtitle
     [:h4.title.is-5 subtitle])])

(defn content [content]
  [:div.prompt-content
   content])

(defn footer
  [& {:keys [previous? next-link next-label] :or {previous? true, next-label "Next"}}]
  [:nav.prompt-controls
   (when previous?
     [previous-button])
   (if next-link
     (next-link* next-link next-label)
     [next-button next-label])])

(defn prompt [header content & [foot]]
  [:div.prompt
   header
   content
   (or foot (footer))])
