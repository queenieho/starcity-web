(ns apply.personal.views
  (:require [apply.prompts.views :as p]
            [apply.personal.models :as m]
            [apply.states :as states]
            [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [starcity.dom :as dom]
            [cljs-time.core :as t]
            [cljs-time.coerce :as c]
            [cljsjs.flatpickr]
            [cljsjs.field-kit]))

;; =============================================================================
;; Phone Number
;; =============================================================================

(defn- phone-number-content [phone-number]
  (r/create-class
   {:display-name "phone-number-content"

    :component-did-mount
    (fn [_]
      (let [elt   (js/document.getElementById "phone-number")
            field (js/FieldKit.TextField. elt (js/FieldKit.PhoneFormatter.))
            attrs {:textDidChange #(reset! phone-number (.value %))}]
        (.setValue field @phone-number)
        (.setDelegate field (clj->js attrs))))

    :reagent-render
    (fn [phone-number]
      [:div.content
       [:p "TODO: Why do we need your phone number?"]
       [:div.form-container
        [:div.form-group
         [:label.label "Enter your phone number below."]
         [:input#phone-number.input
          {:style       {:width "200px"}
           :placeholder "your phone number"
           :type        "tel"}]]]])}))

;; =============================================================================
;; API

(defn phone-number []
  (let [entered-phone-number (subscribe [:personal/phone-number])
        phone-number (r/atom @entered-phone-number)]
    (fn []
      (p/prompt
       (p/header "What is the best phone number to reach you at?")
       (p/content [phone-number-content phone-number])
       (p/footer
        :previous-prompt :logistics/pets
        :next [p/next-button
               :on-click #(dispatch [:prompt/next @phone-number])
               :disabled (not (m/phone-number-complete? @phone-number))])))))

;; =============================================================================
;; Basic Info
;; =============================================================================

;; =============================================================================
;; Internal

(defn- consent-modal [info showing]
  [:div.modal {:class (when @showing "is-active")}
   [:div.modal-background]
   [:div.modal-container
    [:div.modal-content
     [:div.box
      [:div.content
       [:p.title.is-4 "Community Safety Check"]
       [:p "By checking this box, you are providing Starcity written instructions to procure a safety check on you to determine your eligibility for membership and rental capabilities in our community. This safety check is considered an investigative consumer report under California law, and will include a search for criminal records that may be linked to you. All safety checks will be produced in accordance with the Fair Credit Reporting Act and applicable state laws."]
       [:p "You have the right to inquire into whether or not a consumer report was in fact run on you, and, if so, to request the contact information of the consumer reporting agency who furnished the consumer report. You also have the right to view the information that a Consumer Reporting Agency holds on you. You may obtain a copy of this information in person at the Consumer Reporting Agency during regular business hours after providing proper identification and providing reasonable notice for your request. You are allowed to have one additional person accompany you so long as they provide proper identification. Additionally, you can make the same request via the contact information below. The Consumer Reporting Agency can assist you in understanding your file, including coded information."]

       [:ul.is-unstyled
        [:li [:strong "Community Safety"]]
        [:li [:strong "GoodHire, LLC"]]
        [:li "P.O. Box 391146"]
        [:li "Omaha, NE 68139"]
        [:li "855.278.7451"]
        [:li [:a {:href "mailto:support@communitysafety.goodhire.com"} "support@communitysafety.goodhire.com"]]]

       [:button.button.is-success
        {:on-click #(do (swap! info assoc :consent true)
                        (reset! showing false))
         :style {:margin-right "10px"}}
        "Agree"]
       [:button.button.is-danger {:on-click #(reset! showing false)} "Disagree"]]]]]
   [:button.modal-close {:on-click #(reset! showing false)}]])

(defn- consent-group [info]
  (let [showing-modal (r/atom false)]
    (fn [info]
      (let [consent-given? (:consent @info)]
        [:div.form-group
         [:label.label "First, do we have your consent to perform a background check?"]
         [:p.control
          [:label.checkbox
           [:input.checkbox {:type     "checkbox"
                             :checked  consent-given?
                             :on-click (fn [evt]
                                         (if-not consent-given?
                                           (do (.preventDefault evt)
                                               (reset! showing-modal (not @showing-modal)))
                                           (swap! info assoc :consent false)))}]
           "Yes, I authorize Starcity to perform a background check on me."]]

         [consent-modal info showing-modal]]))))

(defn- dob-group [info]
  (r/create-class
   {:display-name "dob-group"

    :component-did-mount
    (fn [_]
      (let [elt (js/document.getElementById "date-picker")]
        (js/window.Flatpickr. elt (clj->js {:altInput    true
                                            :altFormat   "F j, Y"
                                            :defaultDate (:dob @info)
                                            :onChange    #(swap! info assoc :dob %)}))))

    :reagent-render
    (fn [info]
      [:div.form-group
       [:label.label "Great! We'll need to know your birthday."]
       [:div.date-container
        [:input#date-picker.input {:placeholder "choose a date"}]]])}))

(defn- name-group [info]
  (letfn [(-on-change [k]
            #(swap! info assoc-in [:name k] (dom/val %)))]
    (let [{:keys [first middle last]} (:name @info)]
      [:div.form-group
       [:label.label "Our background check will be the most accurate with your full legal name."]
       [:div.control.is-grouped
        ;; First name
        [:p.control.is-expanded
         [:input.input {:value first :on-change (-on-change :first)}]]
        ;; Middle name
        [:p.control.is-expanded
         [:input.input {:placeholder "middle name"
                        :value middle
                        :on-change (-on-change :middle)}]]
        ;; Last Name
        [:p.control.is-expanded
         [:input.input {:value last :on-change (-on-change :last)}]] ]])))

(defn- address-group [info]
  (letfn [(-on-change [k]
            #(swap! info assoc-in [:address k] (dom/val %)))]
    (let [{:keys [zip state city]} (:address @info)]
      [:div.form-group
       [:label.label "Finally, we need to know some basic information about where you live."]
       [:div.control.is-grouped
        ;; City
        [:p.control.is-expanded
         [:input.input {:placeholder "City" :value city :on-change (-on-change :city)}]]
        ;; State
        [:p.control.is-expanded
         [:span.select.is-fullwidth
          [:select {:value (or state "") :on-change (-on-change :state)}
           [:option {:disabled true :value ""} "Choose State"]
           (for [[abbr s] (sort-by val states/states-map)]
             ^{:key (str "state-" abbr)} [:option {:value abbr} s])]]]
        ;; Zipcode
        [:p.control.is-expanded
         [:input.input {:placeholder "Postal Code"
                        :value       zip
                        :type        "number"
                        :min         0
                        :on-change   (-on-change :zip)}]]]])))

(defn- background-check-content [info]
  [:div.content
   [:p "We perform background checks to ensure the safety of our community members. Your background check is "
    [:strong "completely confidential"]
    ", and we'll share the results with you if there are any."]

   [:div.form-container
    [consent-group info]
    (when (:consent @info)
      [:div
       [dob-group info]
       [name-group info]
       [address-group info]])]])

;; =============================================================================
;; API

(defn background-check-info []
  (let [chosen-info (subscribe [:personal/background])
        info        (r/atom @chosen-info)]
    (fn []
      (p/prompt
       (p/header "We need to know a few more things about you. (TODO:)")
       (p/content [background-check-content info])
       (p/footer
        :previous-prompt :logistics/pets
        :next [p/next-button
               :on-click #(dispatch [:prompt/next @info])
               :disabled (not (m/background-complete? @info))])))))

;; =============================================================================
;; Income Verification
;; =============================================================================

(defn- income-verification-content [files]
  (let [complete? (subscribe [:personal.income/complete?])]
    (fn [files]
      [:div.content
       [:p "TODO:"]
       [:div.form-container
        [:div.form-group
         [:label.label
          (if @complete?
            "Your income files have been uploaded. Feel free to upload more if you'd like!"
            "Please upload proof of income.")]
         [:input {:type      "file"
                  :multiple  true
                  :on-change #(reset! files (.. % -currentTarget -files))}]]]])))

(defn- income-verification-complete []
  [:div.content
   [:p "You're all set!"]])

;; =============================================================================
;; API

(defn income-verification []
  (let [uploaded-files (subscribe [:personal/income])
        files          (r/atom nil)]
    (fn []
      (p/prompt
       (p/header "TODO: We need to ensure that you have the income to pay rent.")
       (p/content [income-verification-content files])
       (p/footer
        :previous-prompt :personal/background
        :next [p/next-button
               :on-click #(dispatch [:prompt/next @files])
               :disabled (nil? @files)])))))
