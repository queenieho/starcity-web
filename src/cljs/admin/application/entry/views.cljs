(ns admin.application.entry.views
  (:require [re-frame.core :refer [subscribe dispatch]]
            [starcity.components.layout :as layout]
            [starcity.dates :as dates]
            [clojure.string :as s]
            [cljs-time.coerce :as c]
            [cljs-time.core :as t]
            [reagent.core :as r]
            [starcity.dom :as dom]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- parse-content
  [content keyspec]
  (map (fn [[k label tf]] [label (tf (get content k))]) keyspec))

(defn- key->label
  [k]
  (-> k name (s/replace "-" " ") s/capitalize))

(defn- keyspec
  [& specs]
  (map
   (fn [spec]
     (if (keyword? spec)
       [spec (key->label spec) identity]
       (let [key   (first spec)
             label (if (string? (second spec))
                     (second spec)
                     (key->label key))
             tf    (cond
                     (= (count spec) 3)  (last spec)
                     (fn? (second spec)) (second spec)
                     :otherwise          identity)]
         [key label tf])))
   specs))

;; =============================================================================
;; Components
;; =============================================================================

;; =============================================================================
;; Tab Content

(defn- definition-content*
  [data]
  [:div.content
   [:dl
    (doall
     (for [[title content] data]
       ^{:key title} [:div
                      [:dt title]
                      [:dd content]]))]])

(defn- definition-content
  [sub keyspec]
  (fn []
    (let [content (subscribe sub)]
      (fn []
        [:section.section.is-small
         [definition-content* (parse-content @content keyspec)] ]))))

(def ^:private basic-info
  (definition-content [:application.entry/basic-info]
    (keyspec :email :phone-number :completed-at :address)))

(def ^:private move-in
  (definition-content [:application.entry/move-in]
    (keyspec [:move-in "Desired Move-in"]
             [:properties "Desired Properties"]
             :term)))

(def ^:private community
  (definition-content [:application.entry/community]
    (keyspec [:prior-community-housing "Have you ever lived in communal housing?"]
             [:skills "What skills or traits do you hope to share with the community?"]
             [:why-interested "Why are you interested in Starcity?"]
             [:free-time "How do you spend your free time?"]
             [:dealbreakers "Do you have any dealbreakers?" #(or % "none")])))

;; =====================================
;; Pets

(defmulti pets-keyspec :type)

(defmethod pets-keyspec "cat"
  [data]
  (keyspec :type))

(defmethod pets-keyspec "dog"
  [data]
  (keyspec :type :breed [:weight #(str % "lbs")]))

(defn- pets []
  (let [pet-data (subscribe [:application.entry/pets])]
    (fn []
      [:section.section.is-small
       (if (empty? @pet-data)
         [:div.content [:p "No pets."]]
         [definition-content* (->> (pets-keyspec @pet-data)
                                   (parse-content @pet-data))])])))

;; =====================================
;; Income

(def ^:private $-fmt (partial str "$"))

(defn- income-overview
  [definitions]
  [:div
   [:h3.subtitle "Overview"]
   [definition-content* definitions]])

(defn- income-stream-row
  [{:keys [income period active confidence]}]
  [:tr
   [:td ($-fmt income)]
   [:td period]
   [:td (if active "yes" "no")]
   [:td confidence]])

(defn- income-streams
  [streams]
  [:div
   [:h3.subtitle "Income Streams"]
   [:table.table
    [:thead
     [:tr
      [:th "Income"]
      [:th "Period"]
      [:th "Active?"]
      [:th "Confidence"]]]
    [:tbody
     (map-indexed
      (fn [idx m]
        ^{:key (str "income-stream-" idx)} [income-stream-row m])
      streams)]]])

(defn- bank-account-row
  [{:keys [current-balance available-balance credit-limit type subtype]}]
  [:tr
   [:td ($-fmt current-balance)]
   [:td ($-fmt available-balance)]
   [:td (if-let [amt credit-limit] ($-fmt amt) "N/A")]
   [:td type]
   [:td subtype]])

(defn- bank-accounts
  [accounts]
  [:div
   [:h3.subtitle "Bank Accounts"]
   [:table.table
    [:thead
     [:tr
      [:th "Current Balance"]
      [:th "Available Balance"]
      [:th "Credit Limit"]
      [:th "Type"]
      [:th "Subtype"]]]
    [:tbody
     (map-indexed
      (fn [idx m]
        ^{:key (str "bank-account-" idx)} [bank-account-row m])
      accounts)]]])

(defmulti income-content :type)

(defmethod income-content "plaid"
  [{:keys [last-year last-year-pre-tax projected-yearly streams accounts]}]
  (let [definitions (->> (cond-> []
                           last-year         (conj ["Last Year's Income" last-year])
                           last-year-pre-tax (conj ["Last Year's Income (pre-tax)" last-year-pre-tax])
                           projected-yearly  (conj ["Projected Yearly" projected-yearly]))
                         (map (fn [[t c]] [t ($-fmt c)])))]
    [:div
     (when-not (empty? definitions)
       [:section.section.is-small
        [income-overview definitions]])
     (when streams
       [:section.section.is-small
        [income-streams streams]])
     (when accounts
       [:section.section.is-small
        [bank-accounts accounts]])]))

(defmethod income-content "file"
  [{:keys [files]}]
  [:div
   [:section.section.is-small
    [:div
     [:h3.subtitle "Uploaded Files"]
     [:ul
      (for [{:keys [name file-id]} files]
        ^{:key file-id} [:li [:a {:href     (str "/api/v1/admin/income-file/" file-id)
                                  :download name}
                              name]])]]]])

(defmethod income-content nil [_]
  [:div
   [:section.section.is-small
    [:div
     [:h3.subtitle "No Income Data..."]]]])

(defn- income []
  (let [income-data (subscribe [:application.entry/income])]
    (fn []
      [income-content @income-data])))

;; =====================================
;; Tab Content Dispatch

(defn- tab-content
  "The content that is displayed based on `:active-tab`."
  []
  (let [active-tab (subscribe [:application.entry/active-tab])]
    (fn []
      [:div {:style {:min-height "400px"}}
       (case @active-tab
         :basic-info [basic-info]
         :move-in    [move-in]
         :pets       [pets]
         :community  [community]
         :income     [income])])))

;; =============================================================================
;; Tabs

(defn- tab
  "A single tab."
  [tab-key is-active?]
  [:li {:class (when is-active? "is-active")}
   [:a {:on-click #(dispatch [:application.entry/view-tab tab-key])}
    (-> (name tab-key) s/capitalize)]])

(defn- tabs
  "The tabs that control which portion of application information is presently
  being viewed."
  []
  (let [tabs       (subscribe [:application.entry/tabs])
        active-tab (subscribe [:application.entry/active-tab])]
    (fn []
      [:div.tabs.is-fullwidth
       [:ul
        (doall
         (for [tab-key @tabs]
           ^{:key tab-key} [tab tab-key (= tab-key @active-tab)]))]])))

(defn- customize-email [email-content]
  [:p.control
   [:label.label "Feel free to customize the HTML content of the email:"]
   [:textarea.textarea
    {:value     email-content
     :on-change #(dispatch [:application.entry.approval.email-content/change (dom/val %)])}]])

(defn- approve-modal [showing]
  (let [communities (subscribe [:application.entry.approval/communities])
        selected    (subscribe [:application.entry.approval/selected-community])
        email-content (subscribe [:application.entry.approval/email-content])]
    (fn [showing]
      [:div.modal {:class (when @showing "is-active")}
       [:div.modal-background]
       [:div.modal-card
        [:header.modal-card-head
         [:p.modal-card-title "Are you sure?"]
         [:button.delete {:on-click #(swap! showing not)}]]
        [:div.modal-card-body
         [:div.content
          [:p "This cannot be undone!"]

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

          (when @selected [customize-email @email-content])

          (when @selected
            [:div.control
             [:label.label "Preview"]
             [:div.html-email-preview {:dangerouslySetInnerHTML {:__html @email-content}}]])
          ]]

        [:div.modal-card-foot
         [:button.button.is-success
          {:on-click #(do (swap! showing not)
                          (dispatch [:application.entry/approve @selected]))
           :class    (when-not @selected "is-disabled")}
          "Approve"]
         [:button.button {:on-click #(swap! showing not)}
          "Cancel"]]]])))

(defn approval-button [approved is-showing]
  (let [approving (subscribe [:application.entry/approving?])]
    (fn [approved is-showing]
      (if approved
        [:button.button.is-success.is-disabled
         [:span.icon.is-small {:style {:margin-right "5px"}} [:i.fa.fa-check]]
         "Approved"]
        [:button.button.is-primary
         {:on-click #(swap! is-showing not)
          :class    (when @approving "is-loading")}
         "Approve"]))))

(defn- controls []
  (let [showing-approval (r/atom false)
        approved         (subscribe [:application.entry/approved?])]
    (fn []
      [:div.control.is-grouped
       [:p.control
        [approval-button @approved showing-approval]]
       [approve-modal showing-approval]])))

;; =============================================================================
;; API
;; =============================================================================

(defn application []
  (let [name (subscribe [:application.entry/full-name])]
    (fn []
      [:div.container
       [:h1.title "Member Application"]
       [:h2.subtitle @name]
       [tabs]
       [tab-content]
       [controls]])))
