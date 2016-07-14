(ns starcity.views.application.logistics
  (:require [clj-time
             [coerce :as c]
             [format :as f]]
            [starcity.views.application.common :as common]
            [starcity.views.application.logistics.pets :as pets]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- application-exists?
  [application]
  (not (nil? (:db/id application))))

;; =====================================
;; Date Formatters

(def ^:private view-formatter (f/formatter "MMMM d, y"))
(def ^:private value-formatter (f/formatter :basic-date))

;; =============================================================================
;; Availability

(defn- availability-checkbox
  [chosen idx [date rooms]]
  (let [room-text  (if (> (count rooms) 1) "rooms" "room")
        val        (f/unparse value-formatter (c/from-date date))
        is-chosen? #(-> (chosen %) nil? not)]
    [:label.control.control--checkbox {:for (str "availability-" val)}
     [:input {:id       (str "availability-" val)
              :type     "checkbox"
              :name     "availability[]"
              :value    val
              :required (when (= idx 0) true)
              :data-msg "You must choose at least one available move-in date."
              :checked  (is-chosen? date)}
      [:span (f/unparse view-formatter (c/from-date date))]
      [:span (format " (%s %s available)" (count rooms) room-text)]]
     [:div.control__indicator]]))

(defn- choose-availability
  [application available-units]
  (let [units  (->> available-units (group-by :unit/available-on) (sort-by key))
        chosen (-> application :member-application/desired-availability set)]
    [:div.form-group
     (map-indexed (partial availability-checkbox chosen) units)]))

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
  [application {:keys [db/id available-lease/term available-lease/price]}]
  (let [is-checked (= id (:db/id (:member-application/desired-lease application)))
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
  [{leases :property/available-leases} application]
  [:div.form-group
   (map (partial lease-radio application) leases)])

;; =============================================================================
;; API
;; =============================================================================

(defn logistics
  "Show the logistics page."
  [current-steps application property available-units & {:keys [errors]}]
  (let [sections [["When would you like to move in? Here's what's available:"
                   (choose-availability application available-units)]
                  ["How long would you like to stay? Here are your options:"
                   (choose-lease property application)]
                  ["Do you have pets?" (pets/choose-pet application)]]]
    (common/application
     current-steps
     [:div.question-container
      (common/error-alerts errors)
      [:form {:method "POST"}
       [:input {:type "hidden" :name "property-id" :value (:db/id property)}]
       [:ul.question-list
        (for [[title content] sections]
          (common/section title content))]
       common/onward]]
     :title "Logistics"
     :js ["bower/jquery-validation/dist/jquery.validate.js"
          "validation-defaults.js"
          "logistics.js"])))
