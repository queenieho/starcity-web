(ns starcity.phases.one.sections.basic
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            [reforms.reagent :include-macros true :as f]
            [reforms.validation :include-macros true :as v]
            [re-frame.core :refer [register-handler
                                   register-sub
                                   dispatch
                                   subscribe]]
            [clojure.string :refer [capitalize]]
            [clojure.set :refer [difference]]
            [starcity.phases.common :as phase]
            [starcity.ui.field-kit :as fk]
            [starcity.util :refer [log]]))

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
;; Components: Drivers License

(defn- validate-drivers-license
  [data ui-state]
  (v/validate!
   data ui-state
   (v/present [:number] "Please enter a license number.")))

(defn- drivers-license-group [phase-id]
  (let [data     (r/atom @(subscribe [:basic/drivers-license]))
        ui-state (r/atom {})]
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

(defn- phone-id [i]
  (str "phone-field-" i))

(defn- phone-group [phase-id]
  (let [form-data (r/atom @(subscribe [:basic/phones]))
        ui-state  (r/atom {})
        registry  (fk/make-registry)
        options   (mapv (fn [t] [t (-> t name capitalize)]) PHONE-TYPES)]
    (letfn [(install-fk! []
              (doseq [i (range (count @form-data))]
                (fk/install! registry (phone-id i) fk/phone-formatter)))]
      (r/create-class
       {:display-name         "phone-group"
        :component-did-mount  install-fk!
        :component-did-update install-fk!
        :reagent-render
        (fn []
          (let [selected-types (into #{} (map :type @form-data))]
            (f/panel
             "Phone Numbers"
             (v/form
              ui-state
              (doall
               (for [i (range (count @form-data))]
                 (let [phone-label (str "Phone Number"
                                        (if (= i 0) "" " (optional)"))
                       attrs       {:id (phone-id i)}]
                   [:div.row {:key i}
                    [:div.col-xs-8
                     (v/text attrs phone-label form-data [i :number]
                             :placeholder "(234) 567-8910")]
                    [:div.col-xs-4
                     (v/select "Type" form-data [i :type] options)]])))
              (f/form-buttons
               (f/button-default "Previous" #(phase/navigate! phase-id :basic/ssn))
               (f/button-default "Add Phone" #(swap! form-data conj (make-phone (next-phone-type selected-types))))
               (f/button-primary
                "Next"
                (fn []
                  (when (validate-phones form-data ui-state)
                    (dispatch [:basic.phones/submit! {:phones @form-data}])
                    (phase/navigate! phase-id :basic/drivers-license)))))))))}))))

;; =============================================================================
;; Components: Social Security Number

(defn- validate-ssn
  [data ui-state]
  (v/validate!
   data ui-state
   (v/present [:ssn] "Please enter your social security number.")))

(defn- ssn-group [phase-id]
  (let [data     (r/atom {:ssn @(subscribe [:basic/ssn])})
        ui-state (r/atom {})
        registry (fk/make-registry)]
    (r/create-class
     {:display-name "ssn-group"
      :component-did-mount
      #(fk/install! registry "ssn-field" fk/ssn-formatter)
      :reagent-render
      (fn []
        (f/panel
         "Social Security Number"
         (v/form
          ui-state
          (v/text {:id "ssn-field"} "Social Security Number" data [:ssn]
                  :placeholder "123-45-6789")
          (f/form-buttons
           (f/button-default "Previous" #(phase/navigate! phase-id :basic/name))
           (f/button-primary
            "Next"
            #(when (validate-ssn data ui-state)
               (dispatch [:basic.ssn/submit! @data])
               (phase/navigate! phase-id :basic/phones)))))))})))


;; =============================================================================
;; Components: Name

(defn- validate-name
  [data ui-state]
  (v/validate!
   data ui-state
   (v/present [:first] "Please enter a first name.")
   (v/present [:last] "Please enter a last name.")))

(defn- name-group [phase-id]
  (let [data     (r/atom @(subscribe [:basic/name]))
        ui-state (r/atom {})]
    (fn []
      (f/panel
       "Name"
       (v/form
        ui-state
        (v/text {:id "firstNameField"} "First Name" data [:first])
        (v/text "Middle Name" data [:middle])
        (v/text "Last Name" data [:last])
        (f/form-buttons
         (f/button-primary
          "Next"
          #(when (validate-name data ui-state)
             (dispatch [:basic.name/submit! @data])
             (phase/navigate! phase-id :basic/ssn)))))))))

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
