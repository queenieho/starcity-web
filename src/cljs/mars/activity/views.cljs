(ns mars.activity.views
  (:require [mars.components.antd :as a]
            [re-frame.core :refer [dispatch subscribe]]
            [mars.components.pane :as pane]
            [reagent.core :as r]))

(defn news-item [{:keys [id avatar-url title content action]}]
  [a/card {:style {:margin-bottom "20px"}}
   [:article.media
    [:figure.media-left
     [:p.image.is-32x32
      [:img {:src avatar-url}]]]
    [:div.media-content
     [:div.content.is-medium
      [:p [:strong title]]
      [:p {:dangerouslySetInnerHTML {:__html content}}]
      (when-let [{:keys [link label event]} action]
        [:div
         [:a.button.is-primary
          {:href     link
           :on-click #(dispatch event)}
          label]])]]
    [:div.media-right
     [:button.delete
      {:on-click #(dispatch [:activity.feed.item/dismiss id])}]]]])

(def reload-button
  (r/as-element
   [:a {:on-click #(dispatch [:activity.feed/fetch])}
    [a/icon {:type "reload"}]]))

(defn empty-feed []
  [a/card {:title "No news is good news?" :extra reload-button}
   [:div.content.is-medium
    "Looks like we're all out of updates. Expect more soon!"]])

(defn feed []
  (let [items   (subscribe [:activity.feed/items])
        loading (subscribe [:activity.feed/loading?])]
    (fn []
      [:div
       (cond
         @loading        [a/card {:loading true}]
         (empty? @items) [empty-feed @loading]
         :otherwise      (doall
                          (map (fn [item]
                                 ^{:key (:id item)} [news-item item])
                               @items)))])))

(defn activity []
  [:div.activity
   (pane/pane
    (pane/header "The Latest"
                 "News, updates &amp; more")
    (pane/content [feed]))])
