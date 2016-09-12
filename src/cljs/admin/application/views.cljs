(ns admin.application.views
  (:require [re-frame.core :refer [subscribe dispatch]]
            [starcity.components.layout :as layout]
            [starcity.dates :as dates]
            [clojure.string :as str]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- parse-content
  [content keyspec]
  (map (fn [[k label tf]] [label (tf (get content k))]) keyspec))

(defn- key->label
  [k]
  (-> k name (str/replace "-" " ") str/capitalize))

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
  (definition-content [:application/basic-info]
    (keyspec :email
             :phone-number
             [:completed-at (partial dates/format :full-datetime)]
             :address)))

(def ^:private move-in
  (definition-content [:application/move-in]
    (keyspec [:move-in "Desired Move-in" (partial dates/format :full-datetime)]
             [:properties "Desired Properties" (partial str/join ", ")]
             [:term #(str % " months")])))

(def ^:private community
  (definition-content [:application/community]
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
  (let [pet-data (subscribe [:application/pets])]
    (fn []
      [:section.section.is-small
       [definition-content* (->> (pets-keyspec @pet-data)
                                 (parse-content @pet-data))]])))

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
  (let [income-data (subscribe [:application/income])]
    (fn []
      [income-content @income-data])))

;; =====================================
;; Tab Content Dispatch

(defn- tab-content
  "The content that is displayed based on `:active-tab`."
  []
  (let [active-tab (subscribe [:application/active-tab])]
    (fn []
      (case @active-tab
        :basic-info [basic-info]
        :move-in    [move-in]
        :pets       [pets]
        :community  [community]
        :income     [income]))))

;; =============================================================================
;; Tabs

(defn- tab
  "A single tab."
  [tab-key is-active?]
  [:li {:class (when is-active? "is-active")}
   [:a {:on-click #(dispatch [:application/view-tab tab-key])}
    (-> (name tab-key) str/capitalize)]])

(defn- tabs
  "The tabs that control which portion of application information is presently
  being viewed."
  []
  (let [tabs       (subscribe [:application/tabs])
        active-tab (subscribe [:application/active-tab])]
    (fn []
      [:div.tabs.is-fullwidth
       [:ul
        (doall
         (for [tab-key @tabs]
           ^{:key tab-key} [tab tab-key (= tab-key @active-tab)]))]])))

;; =============================================================================
;; API
;; =============================================================================

(defn application []
  (let [name (subscribe [:application/full-name])]
    (fn []
      [:main
       [:section.section
        [:div.container
         [:h1.title "Member Application"]
         [:h2.subtitle @name]
         [tabs]
         [tab-content]]]])))
