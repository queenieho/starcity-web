(ns starcity.application.basic
  (:require [clojure.set :refer [difference]]
            [re-frame.core
             :refer
             [dispatch register-handler register-sub subscribe]]
            [starcity.components.form :as form])
  (:require-macros [reagent.ratom :refer [reaction]]))

;; =============================================================================
;; Helpers

(defn- make-phone
  ([]
   (make-phone :cell))
  ([type]
   (make-phone type :secondary))
  ([type priority]
   {:priority priority :type type :number ""}))

;; =============================================================================
;; Subscriptions

(register-sub
 :basic-info/name
 (fn [db _]
   (reaction
    (let [{:keys [first middle last]} (get-in @db [:application :personal :basic :name])]
      [first middle last]))))

(register-sub
 :basic-info/ssn
 (fn [db _]
   (reaction (get-in @db [:application :personal :basic :ssn]))))

(register-sub
 :basic-info/phones
 (fn [db _]
   (reaction (get-in @db [:application :personal :basic :phones]))))

(register-sub
 :basic-info/drivers-license
 (fn [db _]
   (reaction
    (let [{:keys [number state]} (get-in @db [:application :personal :basic :drivers-license])]
      [number state]))))

;; =============================================================================
;; Event Handlers

;; TODO:
;; (defn- make-handler
;;   [])

(register-handler
 :basic-info.name/changed!
 (fn [app-state [_ k text]]
   (assoc-in app-state [:application :personal :basic :name k] text)))

(register-handler
 :basic-info.ssn/changed!
 (fn [app-state [_ text]]
   (assoc-in app-state [:application :personal :basic :ssn] text)))

(register-handler
 :basic-info.phone/changed!
 (fn [app-state [_ i k v]]
   (assoc-in app-state [:application :personal :basic :phones i k] v)))

(register-handler
 :basic-info.phone/add!
 (fn [app-state [_ next]]
   (update-in app-state [:application :personal :basic :phones]
              #(conj % (make-phone (or next :cell))))))

(register-handler
 :basic-info.drivers-license/changed!
 (fn [app-state [_ k v]]
   (assoc-in app-state [:application :personal :basic :drivers-license k] v)))

(register-handler
 :basic-info/submit
 (fn [app-state _]
   (.log js/console (clj->js (get-in app-state [:application :personal :basic])))
   app-state))

;; =============================================================================
;; Name

(defn- name-row []
  (let [data (subscribe [:basic-info/name])
        handler (fn [k] #(dispatch [:basic-info.name/changed! k %]))]
    (fn []
      (let [[first middle last] @data]
        [:div.row
         (for [[k lbl v] [[:first "First Name" first]
                          [:middle "Middle Name" middle]
                          [:last "Last Name" last]]]
           ^{:key k} [form/input-group v
                      :label lbl
                      :class "col-sm-4"
                      :input-opts {:placeholder lbl
                                   :on-change (handler k)}])]))))

;; =============================================================================
;; Social & Drivers License

(defn- ssn-row []
  (let [ssn (subscribe [:basic-info/ssn])]
    (fn []
      [:div.row
       [form/input-group @ssn
        :class "col-sm-12"
        :label "Social Security Number"
        :input-opts {:placeholder "e.g. 111-222-3333"
                     :on-change #(dispatch [:basic-info.ssn/changed! %])}]])))

(defn- drivers-license-row []
  (let [data (subscribe [:basic-info/drivers-license])]
    (fn []
      (let [[number state] @data]
        [:div.row

         [form/input-group number
          :label "Driver's License Number"
          :class "col-xs-8"
          :input-opts {:placeholder "Driver's License Number"
                       :on-change   #(dispatch [:basic-info.drivers-license/changed! :number %])}]

         [form/select-group state
          [["California" :ca]]
          :label "State"
          :class "col-xs-4"
          :input-opts {:on-change #(dispatch [:basic-info.drivers-license/changed! :state %])}]]))))

;; =============================================================================
;; Phone

(defn- label-for [{:keys [priority] :as phone}]
  (str "Phone Number" (if (= priority :secondary) " (secondary)" "")))

(defn add-phone-button [selected-types]
  (let [next (first (difference #{:cell :work :home} selected-types))] ; TODO:
                                        ; hard-coded
                                        ; types?
    [:div.row
     [:div.col-lg-12
      [:button.btn.btn-info.pull-right
       {:type     "button"
        :on-click #(dispatch [:basic-info.phone/add! next])}
       "Add Phone"]]]))

(defn- phone-row [idx {:keys [number type priority] :as phone}]
  [:div.row
   [form/input-group number
    :label (label-for phone)
    :class "col-xs-8"
    :input-opts {:placeholder "(234) 567-8910"
                 :on-change #(dispatch [:basic-info.phone/changed! idx :number %])}]

   [form/select-group type
    [["Cell" :cell] ["Home" :home] ["Work" :work]]
    :label "Type"
    :class "col-xs-4"
    :input-opts {:on-change #(dispatch [:basic-info.phone/changed! idx :type %])}]])

(defn- phone-rows []
  (let [phones (subscribe [:basic-info/phones])]
    (fn []
      (let [selected-types (into #{} (map :type @phones))]
        [:div
         (map-indexed (fn [i p] ^{:key i} [phone-row i p]) @phones)
         [add-phone-button selected-types]]))))

;; =============================================================================
;; Form

(defn- form-buttons []
  [:div.row
   [:div.col-lg-12
    [:button.btn.btn-primary {:type "submit"}
     "Submit"]]])

;; =============================================================================
;; API

(defn form
  []
  [:form {:on-submit #(do
                        (dispatch [:basic-info/submit])
                        (.preventDefault %))}
   [name-row]
   [phone-rows]
   [ssn-row]
   [drivers-license-row]
   [form-buttons]])
