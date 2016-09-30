(ns apply.logistics.views
  (:require [apply.prompts.views :as p]
            [apply.routes :refer [prompt-uri]]
            [starcity.dom :as dom]
            [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as r]
            [cljs-time.core :as t]
            [cljs-time.coerce :as c]
            [cljsjs.flatpickr]
            [starcity.log :as l]))

;; =============================================================================
;; Choose Communities
;; =============================================================================

;; =============================================================================
;; Internal

(defn- handle-select-community
  [community-name e]
  (dispatch [:logistics.communities/select community-name (dom/checked e)]))

(defn- community [{:keys [internal-name name num-units available-on]} selections]
  [:p.control
   [:label.checkbox
    [:input {:type            "checkbox"
             :default-checked (selections internal-name)
             :on-click        (partial handle-select-community internal-name)}]
    [:a {:href "#"} name]
    [:span.num-units (str num-units " units open")]
    [:span.availability (str "available " available-on)]]])

;; =============================================================================
;; API

(defn choose-communities []
  (let [selections  (subscribe [:logistics.communities/form-data])
        communities (subscribe [:logistics/available-communities])]
    (fn []
      (p/prompt
       (p/header "Which Starcity communities would you like to join?")
       (p/content
        [:div.content
         [:p "The first thing to do is choose which communities you're interested in living in."]

         [:div.form-container
          [:label.label "Choose communities. TODO: copy"]
          (doall
           (for [c @communities]
             ^{:key (:internal-name c)} [community c @selections]))]])))))

;; =============================================================================
;; Duration of Stay
;; =============================================================================

;; =============================================================================
;; Internal

(defn- label-for [term]
  (cond
    (= term 1)        "Month-to-month"
    (< (/ term 12) 1) (str term " months")
    :otherwise        (str (/ term 12) " year")))

(defn- license-form [selected-license licenses]
  [:div.form-container
   [:label.label "Choose the option that works best for you."]
   [:p.control
    (doall
     (for [{:keys [id term]} licenses]
       ^{:key id}
       [:label.radio
        [:input {:type            "radio"
                 :name            "license"
                 :default-checked (= id selected-license)
                 :value           id
                 :on-change       #(dispatch [:logistics.license/select (-> % dom/val js/parseInt)])}]
        (label-for term)]))]])

(defn- format-price [price]
  (str "$" price "/mo"))

(defn- table-head [selected-license licenses]
  [:thead
   [:tr
    [:th "Community"]
    (for [{:keys [id term]} licenses]
      ^{:key (str "th-" id)}
      [:th {:class (when (= selected-license id) "is-active")}
       (label-for term)])]])

(defn- term-row
  [selected-communities selected-license {:keys [name internal-name prices]}]
  (let [is-selected-community? (selected-communities internal-name)]
    [:tr
     [:td {:class (when is-selected-community? "is-active")} name]
     (for [{:keys [price license-id]} prices]
       ^{:key (str internal-name price)}
       [:td {:class (when (and is-selected-community?
                               (= selected-license license-id))
                      "is-active")}
        (format-price price)])]))

(defn- terms-table [_ _]
  (let [license-prices       (subscribe [:logistics.license/license-prices])
        selected-communities (subscribe [:logistics.communities/form-data])]
    (fn [selected-license licenses]
      [:table.table.is-narrow
       [table-head selected-license licenses]
       [:tbody
        (doall
         (for [row-data @license-prices]
           ^{:key (str "row-" (:internal-name row-data))}
           [term-row @selected-communities selected-license row-data]))]])))


(defn- choose-license-content [selected-license]
  (let [licenses (subscribe [:logistics.license/available-licenses])]
    (fn [selected-license]
      [:div.content
       [:p "We offer three different membership plans that vary from "
        [:strong "most affordable "]
        [:em "(1 year)"]
        ", to "
        [:strong "most flexible "]
        [:em "(month-to-month)"]
        "."]

       [:p "Our rates vary from community to community "
        [:span {:dangerouslySetInnerHTML {:__html "&mdash;"}}]
        " here's a breakdown:"]

       [terms-table selected-license @licenses]

       [license-form selected-license @licenses]])))

;; =============================================================================
;; API

(defn choose-license []
  (let [selected-license (subscribe [:logistics.license/form-data])]
    (fn []
      (p/prompt
       (p/header "How long would you like to stay at Starcity?")
       (p/content [choose-license-content @selected-license])))))

;; =============================================================================
;; Move-in Date
;; =============================================================================

;; =============================================================================
;; Internal

(defn- move-in-date-content [data]
  (let [min-date (c/to-date (t/plus (t/now) (t/weeks 1)))]
    (r/create-class
     {:display-name "move-in-date-content"
      :component-did-mount
      (fn [this]
        (let [elt (js/document.getElementById "date-picker")
              fp  (js/window.Flatpickr.
                   elt
                   (clj->js {:altInput    true
                             :altFormat   "F j, Y"
                             :minDate     min-date
                             :defaultDate (:data (r/props this))
                             :onChange    #(dispatch [:logistics.move-in-date/choose
                                                      (.toISOString %)])}))]
          (r/set-state this {:flatpickr fp})))

      :component-did-update
      (fn [this _]
        (let [fp       (:flatpickr (r/state this))
              new-date (:data (r/props this))]
          (.setDate fp new-date)))

      :reagent-render
      (fn [_]
        [:div.content
         [:p "TODO: copy"]
         [:div.form-container
          [:label.label "Choose your ideal move-in date."]
          [:div.date-container
           [:input#date-picker.input
            {:placeholder "click here"}]]]])})))

;; =============================================================================
;; API

(defn move-in-date []
  (let [move-in-date (subscribe [:logistics.move-in-date/form-data])]
    (fn []
      (p/prompt
       (p/header "When would you like to move in?")
       ;; NOTE: If we want to access props in a lifecycle method with
       ;; `reagent.core/props`, props need to be a map!
       (p/content [move-in-date-content {:data @move-in-date}])))))

;; =============================================================================
;; Pets
;; =============================================================================

;; =============================================================================
;; Internal

(defn- to-bool [yes-or-no]
  (= yes-or-no "yes"))

(defn- has-pet [{has-pet? :has-pet?}]
  [:div.form-group
   [:label.label "Choose..."]
   [:p.control
    (doall
     (for [[label value] [["Yes" "yes"] ["No" "no"]]]
       ^{:key label}
       [:label.radio
        [:input {:type            "radio"
                 :name            "has-pet"
                 :default-checked (= has-pet? (to-bool value))
                 :value           value
                 :on-change       #(dispatch [:logistics.pets/has-pet (-> % dom/val to-bool)])}]
        label]))]])

(defn- pet-type [{:keys [has-pet? pet-type]}]
  (when has-pet?
    [:div.form-group
     [:label.label "What kind of pet do you have?"]
     [:p.control
      (doall
       (for [[label value] [["Dog" "dog"] ["Cat" "cat"] ["Other" "other"]]]
         ^{:key label}
         [:label.radio
          [:input {:type            "radio"
                   :name            "pet-type"
                   :default-checked (= pet-type value)
                   :value           value
                   :on-change       #(dispatch [:logistics.pets/choose-type (dom/val %)])}]
          label]))]]))

(defn- dog-fields [{weight :weight, breed :breed}]
  (letfn [(-on-change [k]
            #(dispatch [(keyword "logistics.pets" (name k)) (dom/val %)]))]
    [:div.form-group
     [:label.label "Please tell us a bit about your dog."]
     [:div.control.is-grouped
      [:p.control.is-expanded
       [:input.input {:placeholder "What breed?"
                      :value       breed
                      :on-change   (-on-change :breed)}]]
      [:p.control.is-expanded
       [:input.input {:type        "number"
                      :placeholder "How much does he/she weigh?"
                      :value       weight
                      :on-change   (-on-change :weight)}]]]]))

(defn- other-pet-fields [{other :other}]
  [:div.form-group
   [:label.label "What kind of pet do you have?"]
   [:input.input {:placeholder "e.g. iguana"
                  :value       other
                  :on-change   #(dispatch [:logistics.pets/other (dom/val %)])}]])

(defn- pet-specific-fields [{:keys [has-pet? pet-type] :as info}]
  (when has-pet?
    (case pet-type
      "dog"   [dog-fields info]
      "other" [other-pet-fields info]
      [:div])))

(defn- pets-content [pet-info]
  [:div.content
   [:p "TODO:"]
   [:div.form-container
    [has-pet pet-info]
    [pet-type pet-info]
    [pet-specific-fields pet-info]]])

;; =============================================================================
;; API

(defn pets []
  (let [pets-info (subscribe [:logistics.pets/form-data])]
    (fn []
      (p/prompt
       (p/header "Do you have any pets?")
       (p/content [pets-content @pets-info])))))
