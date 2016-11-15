(ns admin.application.entry.views.approval
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [starcity.dom :as dom]))

(defn- deposit-amount []
  (let [amount (subscribe [:application.entry.approval/deposit-amount])]
    (fn []
      [:p.control
       [:label.label "How much should the security deposit be? Defaults to first month's rent."]
       [:input.input {:value     @amount
                      :step      1
                      :type      "number"
                      :on-change #(dispatch [:application.entry.approval.deposit/change (dom/val %)])}]])))

(defn- customize-email [email-content]
  [:p.control
   [:label.label "Feel free to customize the HTML content of the email:"]
   [:textarea.textarea
    {:value     email-content
     :on-change #(dispatch [:application.entry.approval.email-content/change (dom/val %)])}]])

(defn customize-subject [email-subject]
  [:p.control
   [:label.label "The subject of the email:"]
   [:input.input {:value     email-subject
                  :type      "text"
                  :on-change #(dispatch [:application.entry.approval.email-subject/change (dom/val %)])}]])

(defn- confirmation-modal [showing selected]
  [:div.modal {:class (when @showing "is-active")}
   [:div.modal-background {:on-click #(reset! showing false)}]
   [:div.box.modal-content.content
    [:h3.title.is-3 "Are you sure?"]
    [:p.is-danger "Please note that " [:strong "this cannot be undone!"]]
    [:button.button.is-danger
     {:on-click #(do
                   (dispatch [:application.entry/approve @selected])
                   (reset! showing false))}
     "Yes, approve it."]]
   [:button.modal-close {:on-click #(reset! showing false)}]])

(defn approval []
  (let [communities          (subscribe [:application.entry.approval/communities])
        selected             (subscribe [:application.entry.approval/selected-community])
        email-content        (subscribe [:application.entry.approval/email-content])
        email-subject        (subscribe [:application.entry.approval/email-subject])
        showing-confirmation (r/atom false)]
    (fn []
      [:div
       [confirmation-modal showing-confirmation selected]
       [:div.content
        [:h3.title.is-3 "Approve"]
        [:label.label "Approve for which property?"]
        [:p.control
         (doall
          (for [[name value] @communities]
            ^{:key value}
            [:label.radio
             [:input {:type      "radio"
                      :name      "license"
                      :value     value
                      :on-change #(dispatch [:application.entry.approval/select-community (dom/val %)])}]
             name]))]

        (when @selected [deposit-amount])

        (when @selected [customize-subject @email-subject])

        (when @selected [customize-email @email-content])

        (when @selected
          [:div.control
           [:label.label "Preview"]
           [:div.html-email-preview {:dangerouslySetInnerHTML {:__html @email-content}}]])]

       (when @selected
         [:button.button.is-success
          {:on-click #(reset! showing-confirmation true)
           :class    (when-not @selected "is-disabled")}
          "Approve"])])))
