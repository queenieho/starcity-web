(ns starcity.sections.basic
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent :refer [atom]]
            [reforms.reagent :include-macros true :as f]
            [reforms.validation :include-macros true :as v]
            [re-frame.core :refer [register-handler
                                   register-sub
                                   dispatch
                                   subscribe]]
            [clojure.string :refer [capitalize]]
            [clojure.set :refer [difference]]))

;; =============================================================================
;; Constants

(def ^:private PHONE-TYPES #{:cell :home :work})

;; =============================================================================
;; Subscriptions

(register-sub
 :basic/name
 (fn [db _]
   (reaction (get-in @db [:basic :name]))))

(register-sub
 :basic/ssn
 (fn [db _]
   (reaction (get-in @db [:basic :ssn]))))

(register-sub
 :basic/phones
 (fn [db _]
   (reaction (get-in @db [:basic :phones]))))

(register-sub
 :basic/drivers-license
 (fn [db _]
   (reaction (get-in @db [:basic :drivers-license]))))

;; =============================================================================
;; Handlers

(register-handler
 :basic.name/submit!
 (fn [app-state [_ data]]
   (dispatch [:progress/next! :basic/name])
   (assoc-in app-state [:basic :name] data)))

(register-handler
 :basic.ssn/submit!
 (fn [app-state [_ {:keys [ssn] :as data}]]
   (dispatch [:progress/next! :basic/ssn])
   (assoc-in app-state [:basic :ssn] ssn)))

(register-handler
 :basic.phones/submit!
 (fn [app-state [_ {:keys [phones] :as data}]]
   (dispatch [:progress/next! :basic/phones])
   (assoc-in app-state [:basic :phones] phones)))

(register-handler
 :basic.drivers-license/submit!
 (fn [app-state [_ data]]
   (dispatch [:progress/next! :basic/drivers-license])
   (assoc-in app-state [:basic :drivers-license] data)))

;; =============================================================================
;; Components: Name

(defn- validate-name
  [data ui-state]
  (when (v/validate!
         data ui-state
         (v/present [:first] "Please enter a first name.")
         (v/present [:last] "Please enter a last name."))
    (dispatch [:basic.name/submit! @data])))

(defn- name-group []
  (let [data     (atom @(subscribe [:basic/name]))
        ui-state (atom {})]
    (fn []
      (f/panel
       "Name"
       (v/form
        ui-state
        (v/text "First Name" data [:first])
        (v/text "Middle Name" data [:middle])
        (v/text "Last Name" data [:last])
        (f/form-buttons
         (f/button-primary "Next" (fn [_] (validate-name data ui-state)))))))))

;; =============================================================================
;; Components: Social Security Number

(defn- validate-ssn
  [data ui-state]
  (when (v/validate!
         data ui-state
         (v/present [:ssn] "Please enter your social security number."))
    (dispatch [:basic.ssn/submit! @data])))

(defn- ssn-group []
  (let [data     (atom {:ssn @(subscribe [:basic/ssn])})
        ui-state (atom {})]
    (fn []
      (f/panel
       "Social Security Number"
       (v/form
        ui-state
        (v/text "Social Security Number" data [:ssn])
        (f/form-buttons
         (f/button-default "Previous" #(dispatch [:progress/previous! :basic/ssn]))
         (f/button-primary "Next" (fn [_] (validate-ssn data ui-state)))))))))

;; =============================================================================
;; Components: Phone Numbers

(defn- validate-phones
  [data ui-state]
  (when (v/validate!
         data ui-state
         (v/present [0 :number] "Please enter a phone number."))
    (dispatch [:basic.phones/submit! {:phones @data}])))

(defn- make-phone
  ([]
   (make-phone :cell))
  ([type]
   (make-phone type :secondary))
  ([type priority]
   {:priority priority :type type}))

(defn- next-phone-type
  [selected-types]
  (first (difference PHONE-TYPES selected-types)))

(defn- phone-group []
  (let [data        (atom @(subscribe [:basic/phones]))
        ui-state    (atom {})
        options     (mapv (fn [t] [t (-> t name capitalize)]) PHONE-TYPES)]
    (fn []
      (let [selected-types (into #{} (map :type @data))]
        (f/panel
         "Phone Numbers"
         (v/form
          ui-state
          (doall
           (for [i (range (count @data))]
             (let [phone-label (str "Phone Number"
                                    (if (= i 0) "" " (optional)"))]
               [:div.row {:key i}
                [:div.col-xs-8
                 (v/text phone-label data [i :number])]
                [:div.col-xs-4
                 (v/select "Type" data [i :type] options)]])))
          (f/form-buttons
           (f/button-default "Previous" #(dispatch [:progress/previous! :basic/phones]))
           (f/button-default "Add Phone" #(swap! data conj (make-phone (next-phone-type selected-types))))
           (f/button-primary "Next" #(validate-phones data ui-state)))))))))

;; =============================================================================
;; Components: Drivers License

(defn- validate-drivers-license
  [data ui-state]
  (when (v/validate!
         data ui-state
         (v/present [:number] "Please enter a license number."))
    (dispatch [:basic.drivers-license/submit! @data])))

(defn- drivers-license-group []
  (let [data     (atom @(subscribe [:basic/drivers-license]))
        ui-state (atom {})]
    (fn []
      (f/panel
       "Drivers License"
       (v/form
        ui-state
        [:div.row
         [:div.col-xs-8
          (v/text "Driver's License Number" data [:number])]
         [:div.col-xs-4
          (v/select "State" data [:state] [[:ca "California"] [:wa "Washington"]])]]
        (f/form-buttons
         (f/button-default "Previous" #(dispatch [:progress/previous! :basic/drivers-license]))
         (f/button-primary "Next" #(validate-drivers-license data ui-state))))))))

;; =============================================================================
;; API

(defn form []
  (let [current-group (subscribe [:progress/group])]
    (fn []
      [:div
       [:h1 "Basic Information"]
       (case @current-group
         :name            [name-group]
         :ssn             [ssn-group]
         :phones          [phone-group]
         :drivers-license [drivers-license-group])])))
