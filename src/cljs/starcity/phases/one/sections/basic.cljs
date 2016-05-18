(ns starcity.phases.one.sections.basic
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent :refer [atom]]
            [reforms.reagent :include-macros true :as f]
            [reforms.validation :include-macros true :as v]
            [re-frame.core :refer [register-handler
                                   register-sub
                                   dispatch
                                   subscribe]]
            [starcity.phases.common :as phase]
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
   (assoc-in app-state [:basic :name] data)))

(register-handler
 :basic.ssn/submit!
 (fn [app-state [_ {:keys [ssn] :as data}]]
   (assoc-in app-state [:basic :ssn] ssn)))

(register-handler
 :basic.phones/submit!
 (fn [app-state [_ {:keys [phones] :as data}]]
   (assoc-in app-state [:basic :phones] phones)))

(register-handler
 :basic.drivers-license/submit!
 (fn [app-state [_ data]]
   (assoc-in app-state [:basic :drivers-license] data)))

;; =============================================================================
;; Components: Name

(defn- validate-name
  [data ui-state]
  (v/validate!
   data ui-state
   (v/present [:first] "Please enter a first name.")
   (v/present [:last] "Please enter a last name.")))

(defn- name-group [phase-id]
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
         (f/button-primary
          "Next"
          #(when (validate-name data ui-state)
             (dispatch [:basic.name/submit! @data])
             (phase/navigate! phase-id :basic/ssn)))))))))

;; =============================================================================
;; Components: Social Security Number

(defn- validate-ssn
  [data ui-state]
  (v/validate!
   data ui-state
   (v/present [:ssn] "Please enter your social security number.")))

(defn- ssn-group [phase-id]
  (let [data     (atom {:ssn @(subscribe [:basic/ssn])})
        ui-state (atom {})]
    (fn []
      (f/panel
       "Social Security Number"
       (v/form
        ui-state
        (v/text "Social Security Number" data [:ssn])
        (f/form-buttons
         (f/button-default "Previous" #(phase/navigate! phase-id :basic/name))
         (f/button-primary
          "Next"
          #(when (validate-ssn data ui-state)
             (dispatch [:basic.ssn/submit! @data])
             (phase/navigate! phase-id :basic/phones)))))))))

;; =============================================================================
;; Components: Phone Numbers

(defn- validate-phones
  [data ui-state]
  (v/validate!
   data ui-state
   (v/present [0 :number] "Please enter a phone number.")))

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

(defn- phone-group [phase-id]
  (let [data     (atom @(subscribe [:basic/phones]))
        ui-state (atom {})
        options  (mapv (fn [t] [t (-> t name capitalize)]) PHONE-TYPES)]
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
           (f/button-default "Previous" #(phase/navigate! phase-id :basic/ssn))
           (f/button-default "Add Phone" #(swap! data conj (make-phone (next-phone-type selected-types))))
           (f/button-primary
            "Next"
            #(when (validate-phones data ui-state)
               (dispatch [:basic.phones/submit! {:phones @data}])
               (phase/navigate! phase-id :basic/drivers-license))))))))))

;; =============================================================================
;; Components: Drivers License

(defn- validate-drivers-license
  [data ui-state]
  (v/validate!
   data ui-state
   (v/present [:number] "Please enter a license number.")))

(defn- drivers-license-group [phase-id]
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
         (f/button-default "Previous" #(phase/navigate! phase-id :basic/phones))
         (f/button-primary
          "Next"
          #(when (validate-drivers-license data ui-state)
             (dispatch [:basic.drivers-license/submit! @data])
             (phase/navigate! phase-id :residence/todo)))))))))

;; =============================================================================
;; API

(defn form []
  (let [phase-id      (subscribe [:phase/current-phase])
        current-group (subscribe [:phase/current-group])]
    (fn []
      [:div
       [:h1 "Basic Information"]
       (case @current-group
         :name            [name-group @phase-id]
         :ssn             [ssn-group @phase-id]
         :phones          [phone-group @phase-id]
         :drivers-license [drivers-license-group @phase-id])])))
