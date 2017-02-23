(ns admin.accounts.views.entry.application
  (:require
   [admin.routes :as routes]
   [admin.components.level :as level]
   [ant-ui.core :as a]
   [cljs-time.coerce :as c]
   [cljs-time.format :as f]
   [re-frame.core :refer [dispatch subscribe]]
   [reagent.core :as r]
   [toolbelt.core :as tb]
   [toolbelt.date :as d]))

;; =============================================================================
;; Overview

(defn- property [p]
  [:a {:href (routes/path-for :property :property-id (:db/id p))}
   (:property/name p)])

(defn overview []
  (let [app (subscribe [:account/application])]
    (fn []
      [a/card
       [level/overview
        (level/overview-item "Status"
                             (:application/status @app)
                             (comp (partial into [:strong]) name))
        (level/overview-item "Created At"
                             (:application/created-at @app)
                             d/short-date-time)
        (level/overview-item "Last Updated"
                             (:application/updated-at @app)
                             d/short-date-time)
        (level/overview-item "Desired Move-in"
                             (:application/move-in @app)
                             d/short-date)
        (level/overview-item "Desired License"
                             (get-in @app [:application/license :license/term])
                             #(str % " months"))
        (level/overview-item "Communities"
                             (:application/communities @app)
                             (fn [cs]
                               (if (empty? cs)
                                 "N/A"
                                 (->> cs
                                      (map-indexed
                                       #(with-meta (property %2) {:key %1}))
                                      (interpose ", ")
                                      (into [:span])))))]])))

;; =============================================================================
;; Community Fitness

(def ^:private fitness-titles
  {:fitness/interested   "Please tell the members why you want to join their community."
   :fitness/free-time    "What do you like to do in your free time?"
   :fitness/dealbreakers "Do you have any dealbreakers?"
   :fitness/experience   "Describe your past experience(s) living in shared spaces."
   :fitness/skills       "How will you contribute to the community?"})

(defn- question-answer [key answer]
  [:div.content
   [:p.title.is-6 (get fitness-titles key)]
   [:p answer]])

(defn community-fitness []
  (let [fitness (subscribe [:account.application/fitness])]
    (fn []
      [a/card {:title "Community Fitness"}
       (if (empty? @fitness)
         [:p "No community fitness questions have been answered."]
         (doall
          (map-indexed
           #(with-meta (question-answer %2 (get @fitness %2)) {:key %1})
           [:fitness/interested
            :fitness/free-time
            :fitness/dealbreakers
            :fitness/experience
            :fitness/skills])))])))

;; =============================================================================
;; Approval

(defn modal-footer []
  (let [can-approve  (subscribe [:approval/can-approve?])
        is-approving (subscribe [:approval/approving?])]
    (fn []
      [:div
       [a/button {:size     :large
                  :on-click #(dispatch [:approval/hide])}
        "Cancel"]
       [a/popconfirm
        {:title       "Are you sure? Undoing it is a pain."
         :ok-text     "Yes"
         :cancel-text "No"
         :on-confirm  #(dispatch [:approval/approve])}
        [a/button {:size     :large
                   :type     :primary
                   :disabled (not @can-approve)
                   :loading  @is-approving}
         "Approve"]]])))

(defn community-selection [choices]
  (let [selection (subscribe [:approval/community])]
    (fn [choices]
      [:div.form-control
       [:label.label "Approve for which community?"]
       [a/radio-group
        {:value     @selection
         :on-change #(dispatch [:approval/update :community (.. % -target -value)])}
        (map-indexed
         (fn [i {:keys [db/id property/name]}]
           ^{:key i} [a/radio {:value id} name])
         choices)]])))

(defn license-selection [name]
  (let [licenses (subscribe [:licenses])
        license  (subscribe [:account.application/license])
        selected (subscribe [:approval/license])]
    (r/create-class
     {:component-will-mount
      (fn [_]
        (do
          (dispatch [:licenses/fetch])
          (dispatch [:approval/update :license (:db/id @license)])))
      :reagent-render
      (fn [name]
        [:div.form-control
         [:label.label "What term does " name " want?"]
         [a/radio-group
          {:defaultValue (:db/id @license)
           :value        @selected
           :on-change    #(dispatch [:approval/update :license (.. % -target -value)])}
          (map-indexed
           (fn [i {:keys [db/id license/term]}]
             ^{:key i} [a/radio {:value id} (str term " months")])
           @licenses)]])})))

(def ^:private date-formatter (f/formatter "yyyy-MM-dd"))

(defn- parse-date [s]
  (f/parse date-formatter s))

(defn move-in-selection [name]
  (let [move-in (subscribe [:account.application/move-in])]
    (r/create-class
     {:component-will-mount
      (fn [_]
        (dispatch [:approval/update :move-in @move-in]))
      :reagent-render
      (fn [name]
        [:div.form-control
         [:label.label "What should be " name "'s move-in date?"]
         [:input.input
          {:style        {:width 300}
           :type         "date"
           :defaultValue (when-let [move-in @move-in]
                           (f/unparse date-formatter (c/to-date-time move-in)))
           :on-change    #(dispatch [:approval/update :move-in (parse-date (.. % -target -value))])}]])})))

(defn- unit-option
  [{:keys [db/id unit/name unit/available unit/market unit/account unit/rate]}]
  [a/select-option {:value (str id) :disabled (not available)}
   [:span
    [:span name]
    [:span {:dangerouslySetInnerHTML {:__html "&middot;"}
            :style                   {:margin "0 3px"}}]
    [:em
     (if-let [occupant (:account/name (and (not available) account))]
       (str "(" occupant " @ $" rate ")")
       (str "$" market))]]])

(defn unit-selection [name]
  (let [units    (subscribe [:approval/units])
        selected (subscribe [:approval/unit])]
    (fn [name]
      [:div.form-control
       [:label.label "Which unit are we putting " name " in?"]
       [a/select
        {:style       {:width 300}
         :placeholder "Select unit"
         :value       (str @selected)
         :on-change   #(dispatch [:approval/update :unit (tb/str->int %)])}
        (map-indexed
         #(with-meta (unit-option %2) {:key %1})
         @units)]])))

(defn approval-modal []
  (let [visible      (subscribe [:approval/showing?])
        account-name (subscribe [:account/name])
        application  (subscribe [:account/application])
        form-data    (subscribe [:approval/form-data])]
    (fn []
      [a/modal {:title     (str "Approve " @account-name)
                :visible   @visible
                :footer    (r/as-element [modal-footer])
                :on-cancel #(dispatch [:approval/hide])}
       [community-selection (:application/communities @application)]
       (when (:community @form-data)
         [license-selection @account-name])
       (when (:license @form-data)
         [move-in-selection @account-name])
       (when (:move-in @form-data)
         [unit-selection @account-name])])))

;; =============================================================================
;; Eligibility

(defn- pet-text [{:keys [pet/type pet/breed pet/weight]}]
  (if (#{:dog} type)
    (str "Dog: " breed ", " weight "lbs")
    (name type)))

(defn- income-file [{:keys [db/id income-file/name]}]
  [:li
   [:a {:href     (str "/api/v1/admin/income-file/" id)
        :download name}
    name]])

(defn approve-button []
  [:a {:on-click #(dispatch [:approval/show])}
   "Approve"])

(defn eligibility []
  (let [app        (subscribe [:account/application])]
    (fn []
      (let [{:keys [application/address application/has-pet
                    application/pet income/files]} @app]
        [:div
         [approval-modal]
         [a/card {:title "Eligiblity"
                  :extra (r/as-element [approve-button])}
          [:div
           [:p [:strong "Current Address"]]
           [:p (or address "N/A")]
           [:p {:style {:margin-top 6}} [:strong "Pet"]]
           [:p (cond
                 (nil? has-pet)   "N/A"
                 (false? has-pet) "No pet."
                 :otherwise       (pet-text pet))]
           [:p {:style {:margin-top 6}} [:strong "Income"]]
           (if (empty? files)
             [:p "N/A"]
             [:ul
              (map-indexed
               #(with-meta (income-file %2) {:key %1})
               files)])]]]))))
