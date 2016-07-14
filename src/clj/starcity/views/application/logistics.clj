(ns starcity.views.application.logistics
  (:require [clj-time.format :as f]
            [clj-time.coerce :as c]
            [starcity.time :refer [next-twelve-months]]
            [starcity.views.application.common :as common]
            [starcity.views.application.logistics.pets :as pets]
            [clj-time.core :as t]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- application-exists?
  [application]
  (not (nil? (:db/id application))))

;; =====================================
;; Date Formatters

(def ^:private month-year-formatter (f/formatter "MMMM y"))
(def ^:private month-day-year-formatter (f/formatter "MMMM d, y"))
(def ^:private value-formatter (f/formatter :basic-date))

;; =============================================================================
;; Properties

(defn- property-checkbox
  [chosen idx {:keys [:db/id :property/name :property/available-on :property/units]}]
  (let [is-chosen?        #(-> (chosen %) nil? not)
        dt                (c/from-date available-on)
        availability-text (if (t/after? (t/now) dt)
                            "now"
                            (f/unparse month-day-year-formatter dt))]
    [:label.control.control--checkbox {:for id}
     [:input {:id       id
              :type     "checkbox"
              :name     "properties[]"
              :value    id
              :required (when (= idx 0) true)
              :data-msg "You must choose at least one available move-in date."
              :checked  (is-chosen? id)}
      [:span (format "%s, %s units available, starting %s" name units availability-text)]]
     [:div.control__indicator]]))

(defn- choose-properties
  [properties desired-properties]
  (let [properties (sort-by :property/available-on properties)]
    [:div.form-group
     (map-indexed (partial property-checkbox (->> desired-properties (map :db/id) set))
                  properties)])) ; sort-by availability?

;; =============================================================================
;; Availability

(defn- choose-availability
  [desired-availability]
  (let [months (next-twelve-months)
        id     "availability"]
    [:div.form-group
     [:select.form-control {:id       id
                            :name     id
                            :value    ""
                            :required true}
      [:option {:value    ""
                :disabled true
                :selected (nil? desired-availability)}
       "-- Select Desired Availability --"]
      [:option {:value (f/unparse value-formatter (first months))} "ASAP"]
      (for [dt (rest months)]
        [:option {:value    (f/unparse value-formatter dt)
                  :selected (= (c/to-date dt) desired-availability)}
         (f/unparse month-year-formatter dt)])]]))

;; =============================================================================
;; Lease

(defn- lease-text
  [term price]
  (let [rem (mod term 12)]
    (cond
      (= term 1)  (format "Month-to-month - $%.0f" price)
      (even? rem) (format "%d year - $%.0f" (/ term 12) price)
      :otherwise  (format "%d month - $%.0f" term price))))

(defn- lease-radio
  [desired-lease {:keys [db/id available-lease/term available-lease/price]}]
  (let [is-checked (= id (:db/id desired-lease))
        radio-id   (str "selected-lease-" id)]
    [:label.control.control--radio {:for radio-id} (lease-text term price)
     [:input {:type     "radio"
              :name     "selected-lease"
              :id       radio-id
              :value    id
              :checked  is-checked
              :data-msg "You must choose one of these options."
              :required true}]
     [:div.control__indicator]]))

(defn- choose-lease
  [leases desired-lease]
  [:div.form-group
   (map (partial lease-radio desired-lease) leases)])

;; =============================================================================
;; API
;; =============================================================================

(defn logistics
  "Show the logistics page."
  [current-steps properties application available-leases & {:keys [errors]}]
  (let [sections [["Which Starcity communities are you applying to?"
                   (choose-properties properties (:member-application/desired-properties application))]
                  ["When is your ideal move-in date?"
                   (choose-availability (:member-application/desired-availability application))]
                  ["How long would you like to stay? Here are your options:"
                   (choose-lease available-leases (:member-application/desired-lease application))]
                  ["Do you have pets?" (pets/choose-pet application)]]]
    (common/application
     current-steps
     [:div.question-container
      (common/error-alerts errors)
      [:form {:method "POST"}
       ;; [:input {:type "hidden" :name "property-id" :value (:db/id property)}]
       [:ul.question-list
        (for [[title content] sections]
          (common/section title content))]
       common/onward]]
     :title "Logistics"
     :js ["bower/jquery-validation/dist/jquery.validate.js"
          "validation-defaults.js"
          "logistics.js"])))
