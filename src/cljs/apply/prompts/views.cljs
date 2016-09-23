(ns apply.prompts.views
  (:require [apply.routes :refer [prompt-uri]]
            [starcity.components.icons :as i]
            [re-frame.core :refer [dispatch subscribe]])
  (:refer-clojure :exclude [next]))

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

(defn next-button
  [& {:keys [label on-click disabled]
      :or   {label    "Next"
             on-click #(dispatch [:prompt/next {}])}}]
  (let [loading? (subscribe [:prompt/loading])]
    (fn [& {:keys [label on-click disabled]
           :or   {label    "Next"
                  on-click #(dispatch [:prompt/next {}])}}]
      [:a.next.is-medium.button.is-primary.is-pulled-right
       {:on-click on-click
        :class    (str (when-not (nil? disabled)
                         (when disabled "is-disabled"))
                       (when @loading? " is-loading"))}
       [:span label]
       (i/angle-right)])))

(defn next-link
  [next-prompt & [label]]
  [:a.next.is-medium.button.is-primary.is-pulled-right
   {:href (prompt-uri next-prompt)}
   [:span (or label "Next")]
   (i/angle-right)])

(defn footer
  [& {:keys [previous-prompt next]}]
  [:nav.prompt-controls
   (when previous-prompt
     [:a.button.is-medium.previous {:href (prompt-uri previous-prompt)}
      (i/angle-left)
      [:span "Previous"]])
   (when next
     next)])

(defn prompt [header content footer]
  [:div.prompt
   header
   content
   footer])
