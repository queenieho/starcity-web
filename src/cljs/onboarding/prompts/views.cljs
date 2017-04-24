(ns onboarding.prompts.views
  (:require [ant-ui.core :as a]
            [onboarding.prompts.content :as content]
            [onboarding.prompts.deposit.method]
            [onboarding.prompts.deposit.bank]
            [onboarding.prompts.deposit.verify]
            [onboarding.prompts.deposit.pay]
            [onboarding.prompts.services.moving]
            [onboarding.prompts.services.storage]
            [onboarding.prompts.services.customize]
            [onboarding.prompts.services.cleaning]
            [onboarding.prompts.services.upgrades]
            [re-frame.core :refer [dispatch subscribe]]))

(def ^:private advisor-image
  [:img.is-circular
   {:src   "/assets/img/meg.jpg"
    :alt   "community advisor headshot"
    :class "community-advisor"}])

(defn prompt-header []
  (let [title (subscribe [:prompt/title])]
    [:header
     [:figure.image.is-64x64
      [:a {:on-click #(dispatch [:help/toggle])} advisor-image]]
     [:h3.prompt-title.title.is-4 @title]]))

(defn- previous-button [active]
  [a/button {:type      :ghost
             :size      :large
             :icon      :left
             :html-type :button
             :on-click  #(dispatch [:prompt/previous active])}
   "Previous"])

(defn- next-button []
  (let [dirty       (subscribe [:prompt/dirty?])
        can-advance (subscribe [:prompt/can-advance?])
        is-saving   (subscribe [:prompt/saving?])]
    [a/button {:type      :primary
               :size      :large
               :disabled  (not @can-advance)
               :loading   @is-saving
               :html-type :submit}
     (if @dirty
       [:span {:dangerouslySetInnerHTML {:__html "Save &amp; Continue"}}]
       "Continue")]))

;; (defn- save-button []
;;   (let [dirty     (subscribe [:prompt/dirty?])
;;         is-saving (subscribe [:prompt/saving?])]
;;     [a/button {:type     :ghost
;;                :size     :large
;;                :disabled (not @dirty)
;;                :loading  @is-saving
;;                :on-click #(dispatch [:prompt/save ])}
;;      "Save"]))

(defn prompt-footer [active]
  (let [has-previous (subscribe [:prompt/has-previous?])]
    [:div.columns.is-mobile.prompt-controls
     (when @has-previous
       [:div.column.has-text-left
        [previous-button active]])
     [:div.column
      [:div.is-pulled-right
       [next-button]]]]))

(defn prompt []
  (let [active (subscribe [:prompt/active])]
    [:form
     {:on-submit #(do
                    (.preventDefault %)
                    (dispatch [:prompt/continue (:keypath @active)]))}
     [:div.prompt
      [prompt-header]
      [:div.prompt-content
       (content/content @active)]
      [prompt-footer (:keypath @active)]]]))
