(ns admin.application.entry.views
  (:require [admin.application.entry.views.approval :refer [approval]]
            [clojure.string :refer [capitalize]]
            [re-frame.core :refer [dispatch subscribe]]
            [starcity.components.loading :as loading]))

;; =============================================================================
;; Components
;; =============================================================================

;; =============================================================================
;; Helpers

(defn- term-text [term]
  (if (= 1 term) (str term " month")
      (str term " months")))

;; =============================================================================
;; Title & Overview

(defn- name-link [name]
  (let [account-id (subscribe [:application.entry/account-id])]
    (fn [name]
      [:a {:href (str "/admin/accounts/" @account-id)} [:b name]])))

(defn- title-bar []
  (let [full-name    (subscribe [:application.entry/full-name])
        completed-at (subscribe [:application.entry/completed-at])
        status       (subscribe [:application.entry/status])
        move-in      (subscribe [:application.entry/desired-move-in])
        term         (subscribe [:application.entry/term])]
    (fn []
      [:div
       [:h2.title.is-2 "Member Application for " [name-link @full-name]]
       [:nav.level.is-mobile {:style {:margin-top "24px"}}
        (when @status
          [:div.level-item.has-text-centered
           [:p.heading "Status"]
           [:p.title [:b (name @status)]]])
        (when @completed-at
          [:div.level-item.has-text-centered
           [:p.heading "Completed At"]
           [:p.title @completed-at]])
        (when @move-in
          [:div.level-item.has-text-centered
           [:p.heading "Desired Move-in"]
           [:p.title @move-in]])
        (when @term
          [:div.level-item.has-text-centered
           [:p.heading "Desired Term"]
           [:p.title (term-text @term)]])]])))

;; =============================================================================
;; Menu

(defn- menu-item [active item-key & {:keys [label disabled complete]}]
  (let [label (or label (capitalize (name item-key)))]
    [:li [:a {:on-click #(dispatch [:application.entry.menu/select-tab item-key])
              :class    (str (when (= active item-key) "is-active")
                             (when disabled " is-disabled"))}
          label
          (when complete
            [:span.is-pulled-right.icon.is-small [:i.fa.fa-check]])]]))

(defmulti menu-action-disabled?
  "Returns true if this menu action should be disabled."
  (fn [item-key _] item-key))

(defmethod menu-action-disabled? :approve
  [_ status]
  (#{:in-progress :approved :rejected} status))

(defmethod menu-action-disabled? :reject
  [_ status]
  (#{:approved :in-progress} status))

(defmethod menu-action-disabled? :default [_ _]
  false)

(defn menu []
  (let [info-tabs   (subscribe [:application.entry.menu/info-tabs])
        action-tabs (subscribe [:application.entry.menu/action-tabs])
        active-tab  (subscribe [:application.entry.menu/active])
        status      (subscribe [:application.entry/status])
        is-approved (subscribe [:application.entry/approved?])
        is-complete (subscribe [:application.entry/complete?])]
    (fn []
      [:aside.menu
       [:p.menu-label "Information"]
       [:ul.menu-list
        (doall
         (for [{:keys [key label complete]} @info-tabs]
           ^{:key key} [menu-item @active-tab key
                        :label label
                        :complete complete]))]
       [:p.menu-label "Actions"]
       [:ul.menu-list
        (doall
         (for [{:keys [key label]} @action-tabs]
           ^{:key key} [menu-item @active-tab key
                        :disabled (menu-action-disabled? key @status)
                        :label label]))]])))

;; =============================================================================
;; Information Views

(def ^:private cf-labels
  {:prior-community-housing "Have you ever lived in communal housing?"
   :skills                  "What skills or traits do you hope to share with the community?"
   :why-interested          "Why are you interested in Starcity?"
   :free-time               "How do you spend your free time?"
   :dealbreakers            "Do you have any dealbreakers?"})

(defn- community-fitness []
  (let [cf-keys [:prior-community-housing :skills :why-interested :free-time :dealbreakers]
        cf-data (subscribe [:application.entry/community-fitness])]
    (fn []
      [:div.content
       (doall
        (for [k cf-keys]
          ^{:key k}
          [:div {:style {:margin-bottom "20px"}}
           [:h3 [:b (get cf-labels k)]]
           [:p (get @cf-data k "N/A")]]))])))

(defn- pet-text [{:keys [has-pet type weight breed]}]
  (cond
    (false? has-pet) "None"
    (#{"dog"} type)  (str weight "lb " breed " (dog)")
    (#{"cat"} type)  "Cat"
    :otherwise       "N/A"))

(defn- move-in []
  (let [cs           (subscribe [:application.entry/communities])
        term         (subscribe [:application.entry/term])
        move-in-date (subscribe [:application.entry/desired-move-in])
        pet          (subscribe [:application.entry/pet])]
    (fn []
      [:div.columns
       [:div.column.is-two-thirds
        [:div.content
         [:h3.title.is-3 "Chosen Communities"]
         [:ul
          (if (empty? @cs)
            [:b "None chosen yet"]
            (for [c @cs] ^{:key c} [:li c]))]]]
       [:div.column
        [:div.content.box
         [:dl
          [:dt "Desired Move-in date"]
          [:dd (or @move-in-date "N/A")]
          [:dt "Desired Term"]
          [:dd (or (term-text @term) "N/A")]
          [:dt "Pet?"]
          [:dd (pet-text @pet)]]]]])))

(defn- eligibility []
  (let [income       (subscribe [:application.entry/income])
        curr-address (subscribe [:application.entry/address])]
    (fn []
      (let [{:keys [type files]} @income]
        [:div.content
         [:h3.title.is-3 "Income"]
         (cond
           (#{"file"} type)  [:ul
                              (for [{:keys [name file-id]} files]
                                ^{:key file-id}
                                [:li [:a {:href     (str "/api/v1/admin/income-file/" file-id)
                                          :download name} name]])]
           (not (nil? type)) [:b "Legacy income. Contact Josh for details."]
           :otherwise        [:p "N/A"])
         [:h3.title.is-3 "Current Address"]
         [:p (or @curr-address "N/A")]]))))

;; =============================================================================
;; Reject

(defn- reject []
  (let [is-rejected  (subscribe [:application.entry/rejected?])
        is-rejecting (subscribe [:application.entry/rejecting?])]
    (fn []
      [:div.content
       [:h3.title.is-3 "Would you like to reject this application?"]
       [:p "Rejecting an application does not result in any auto-generated
       email. That's up to you."]
       [:p [:b "However, "] "should the user log in to Starcity, he/she will be
       told that his/her application has been rejected and to contact us for details."]
       [:p "You " [:b "can"] " un-reject an application."]
       [:button.button.is-danger
        {:class    (when @is-rejecting "is-loading")
         :on-click #(dispatch [:application.entry/reject])}
        (if @is-rejected
          "Un-reject this Application"
          "Reject this Application")]])))

;; =============================================================================
;; API
;; =============================================================================

(defn application []
  (let [is-loading (subscribe [:application.entry/loading?])
        active-tab (subscribe [:application.entry.menu/active])]
    (fn []
      (if @is-loading
        (loading/fill-container "fetching application data...")
        [:div.container
         [title-bar]
         [:hr]
         [:div.columns
          [:div.column.is-one-quarter
           [menu]]
          [:div.column
           (case @active-tab
             :move-in           [move-in]
             :community-fitness [community-fitness]
             :eligibility       [eligibility]
             :approve           [approval]
             :reject            [reject]
             [:p [:b (str "Error: No view for " @active-tab)]])]]]))))
