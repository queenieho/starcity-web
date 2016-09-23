(ns apply.logistics.views
  (:require [apply.prompts.views :as p]
            [apply.logistics.models :refer [pets-complete?]]
            [apply.routes :refer [prompt-uri]]
            [starcity.dom :as dom]
            [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as r]
            [clojure.set :as set]
            [cljs-time.core :as t]
            [cljs-time.coerce :as c]
            [cljsjs.flatpickr]))

;; =============================================================================
;; Choose Communities
;; =============================================================================

;; =============================================================================
;; Internal

(defn- handle-toggle-community [selections internal-name evt]
  (let [checked? (dom/val evt)]
    (if checked?
      (swap! selections conj internal-name)
      (swap! selections set/difference #{internal-name}))))

(defn- community [{:keys [internal-name name num-units available-on]} selections]
  [:p.control
   [:label.checkbox
    [:input {:type            "checkbox"
             :default-checked (@selections internal-name)
             :on-click        (partial handle-toggle-community selections internal-name)}]
    [:a {:href "#"} name]
    [:span.num-units (str num-units " units open")]
    [:span.availability (str "available " available-on)]]])

;; =============================================================================
;; API

(defn choose-communities []
  (let [chosen     (subscribe [:logistics/communities])
        selections (r/atom @chosen)]
    (fn []
      (p/prompt
       (p/header "Which Starcity communities would you like to join?")
       (p/content
        [:div.content
         [:p "The first thing to do is choose which communities you're interested in living in."]

         [:div.form-container
          [:label.label "Choose communities. TODO: copy"]
          [community {:internal-name "52gilbert"
                      :name          "West SoMa"
                      :num-units     5
                      :available-on  "now"} selections]
          [community {:internal-name "2072mission"
                      :name          "The Mission"
                      :num-units     15
                      :available-on  "now"} selections]]])
       (p/footer
        :previous-prompt :overview/advisor
        :next [p/next-button
               :on-click #(dispatch [:prompt/next @selections])
               :disabled (empty? @selections)])))))

;; =============================================================================
;; Duration of Stay
;; =============================================================================

;; =============================================================================
;; Internal

(defn- choose-term [selected-term]
  [:div.form-container
   [:label.label "Choose the option that works best for you."]
   [:p.control
    (doall
     (for [[content value] [["1 year" 12] ["6 months" 6] ["Month-to-month" 1]]]
       ^{:key (str "term-radio-" value)}
       [:label.radio
        [:input {:type            "radio"
                 :name            "term"
                 :default-checked (= value @selected-term)
                 :value           value
                 :on-change       #(reset! selected-term (-> % dom/val js/parseInt))}]
        content]))]])

(defn- format-rate [rate]
  (str "$" rate "/mo"))

;; TODO: Pull community rates from server
(defn- terms-table []
  (let [data [["Month-to-month" 2300 2400]
              ["6 months" 2100 2200]
              ["1 year" 2000 2100]]]
    [:table.table.is-narrow
     [:thead
      [:tr
       [:th "Plan"]
       [:th "West SoMa"]
       [:th "The Mission"]]]
     [:tbody
      (for [[plan rate1 rate2] data]
        ^{:key plan} [:tr
                      [:td plan]
                      [:td (format-rate rate1)]
                      [:td (format-rate rate2)]])]]))

(defn- choose-plan-content [selected-term]
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

   [terms-table]

   [choose-term selected-term]])

;; =============================================================================
;; API

(defn choose-plan []
  (let [chosen-term   (subscribe [:logistics/term])
        selected-term (r/atom @chosen-term)]
    (fn []
      (p/prompt
       (p/header "How long would you like to stay at Starcity?")
       (p/content (choose-plan-content selected-term))
       (p/footer
        :previous-prompt :logistics/communities
        :next [p/next-button
               :on-click #(dispatch [:prompt/next @selected-term])
               :disabled (nil? @selected-term)])))))

;; =============================================================================
;; Move-in Date
;; =============================================================================

;; =============================================================================
;; Internal

(defn- move-in-date-content [move-in-date]
  (let [min-date (c/to-date (t/plus (t/now) (t/weeks 1)))]
    (r/create-class
     {:display-name "move-in-date-content"

      :component-did-mount
      (fn [_]
        (let [elt (js/document.getElementById "date-picker")]
          (js/window.Flatpickr. elt (clj->js {:altInput    true
                                              :altFormat   "F j, Y"
                                              :minDate     min-date
                                              :defaultDate (when-let [d @move-in-date] d)
                                              :onChange    #(reset! move-in-date %)}))))

      :reagent-render
      (fn [move-in-date]
        [:div.content
         [:p "TODO: copy"]
         [:div.form-container
          [:label.label "Choose your ideal move-in date."]
          [:div.date-container
           [:input#date-picker.input {:placeholder "click here"}]]]])})))

;; =============================================================================
;; API

(defn move-in-date []
  (let [chosen-move-in-date (subscribe [:logistics/move-in-date])
        move-in-date        (r/atom @chosen-move-in-date)]
    (fn []
      (p/prompt
       (p/header "When would you like to move in?")
       (p/content [move-in-date-content move-in-date])
       (p/footer
        :previous-prompt :logistics/term
        ;; TODO: Why can't I call to-string on the date?
        :next [p/next-button
               :on-click #(dispatch [:prompt/next (.toISOString @move-in-date)])
               :disabled (nil? @move-in-date)])))))

;; =============================================================================
;; Pets
;; =============================================================================

;; =============================================================================
;; Internal

(defn- to-bool [yes-or-no]
  (= yes-or-no "yes"))

(defn- has-pet [pet-info]
  [:div.form-group
   [:label.label "Choose..."]
   [:p.control
    (doall
     (for [[label value] [["Yes" "yes"] ["No" "no"]]]
       ^{:key label} [:label.radio
                      [:input {:type            "radio"
                               :name            "has-pet"
                               :default-checked (= (:has-pet? @pet-info) (to-bool value))
                               :value           value
                               :on-change       #(swap! pet-info assoc :has-pet? (-> % dom/val to-bool))}]
                      label]))]])

(defn- pet-type [pet-info]
  (when (:has-pet? @pet-info)
    [:div.form-group
     [:label.label "What kind of pet do you have?"]
     [:p.control
      (doall
       (for [[label value] [["Dog" "dog"] ["Cat" "cat"] ["Other" "other"]]]
         ^{:key label} [:label.radio
                        [:input {:type            "radio"
                                 :name            "pet-type"
                                 :default-checked (= (:pet-type @pet-info) value)
                                 :value           value
                                 :on-change       #(swap! pet-info assoc :pet-type (dom/val %))}]
                        label]))]]))

(defn- dog-fields [pet-info]
  (letfn [(-on-change [k]
            #(swap! pet-info assoc k (dom/val %)))]
    [:div.form-group
     [:label.label "Please tell us a bit about your dog."]
     [:div.control.is-grouped
      [:p.control.is-expanded
       [:input.input {:placeholder "What breed?"
                      :value       (:breed @pet-info)
                      :on-change   (-on-change :breed)}]]
      [:p.control.is-expanded
       [:input.input {:type        "number"
                      :placeholder "How much does he/she weigh?"
                      :value       (:weight @pet-info)
                      :on-change   (-on-change :weight)}]]]]))

(defn- other-pet-fields [pet-info]
  [:div.form-group
   [:label.label "What kind of pet do you have?"]
   [:input.input {:placeholder "e.g. iguana"
                  :value       (:other @pet-info)
                  :on-change   #(swap! pet-info assoc :other (dom/val %))}]])

(defn- pet-specific-fields [pet-info]
  (when (:has-pet? @pet-info)
    (case (:pet-type @pet-info)
      "dog"   [dog-fields pet-info]
      "other" [other-pet-fields pet-info]
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
  (let [pet-data (subscribe [:logistics/pets])
        pet-info (r/atom @pet-data)]
    (fn []
      (p/prompt
       (p/header "Do you have any pets?")
       (p/content [pets-content pet-info])
       (p/footer
        :previous-prompt :logistics/move-in-date
        :next [p/next-button
               :on-click #(dispatch [:prompt/next @pet-info])
               :disabled (not (pets-complete? @pet-info))])))))
