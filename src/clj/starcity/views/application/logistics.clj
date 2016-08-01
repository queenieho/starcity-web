(ns starcity.views.application.logistics
  (:require [clj-time.format :as f]
            [clj-time.coerce :as c]
            [starcity.time :refer [next-twelve-months]]
            [starcity.views.application.common :as common]
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
  [chosen idx {:keys [:db/id :property/name :property/upcoming :property/available-on :property/units]}]
  (let [is-chosen?        #(-> (chosen %) nil? not)
        dt                (c/from-date available-on)
        availability-text (cond
                            upcoming              upcoming
                            (t/after? (t/now) dt) "now"
                            :otherwise            (f/unparse month-day-year-formatter dt))]
    [:p; TODO: .tooltipped {:data-position "left" :data-tooltip  "woot!"}
     [:input {:id       id
              :type     "checkbox"
              :name     "properties[]"
              :value    id
              :required (when (= idx 0) true)
              :data-msg "Please choose at least one community."
              :checked  (is-chosen? id)}
      [:label.property-label {:for id}
       [:span.property-name name]
       (let [text (if (zero? units) "full" (format "%s units available" units))]
         [:span.property-units {:class (when (zero? units) "full")}
          text])
       [:span.license-price {:style "display: none;"} ""]
       [:span.availability-date availability-text]]]]))

(defn- choose-properties
  [properties desired-properties]
  (let [properties (sort-by :property/available-on properties)]
    [:div#properties-section.validation-group
     (map-indexed (partial property-checkbox (->> desired-properties (map :db/id) set))
                  properties)])) ; sort-by availability?

;; =============================================================================
;; Availability

(defn- choose-availability
  [desired-availability]
  (let [months (next-twelve-months)]
    [:div.input-field
     [:select {:name     "availability"
               :value    ""
               :required true
               :data-msg "Please choose a move-in date."}
      [:option {:value    ""
                :disabled true
                :selected (nil? desired-availability)}
       "Choose a move-in date"]
      [:option {:value (f/unparse value-formatter (first months))} "ASAP"]
      (for [dt (rest months)]
        [:option {:value    (f/unparse value-formatter dt)
                  :selected (= (c/to-date dt) desired-availability)}
         (f/unparse month-year-formatter dt)])]]))

;; =============================================================================
;; License

(defn- license-term-text
  [term]
  (cond
    (= term 1)        "Month-to-month"
    (< (/ term 12) 1) (format "%d months" term)
    :otherwise        (format "%d year" (/ term 12))))

(defn- license-radio
  [desired-license {:keys [:db/id :license/term]}]
  (let [is-checked (= id (:db/id desired-license))
        radio-id   (str "selected-license-" id)]
    [:p
     [:input {:type     "radio"
              :name     "selected-license"
              :id       radio-id
              :value    id
              :checked  is-checked
              :data-msg "Please choose the duration of your stay."
              :required true}]
     [:label.license-label {:for radio-id}
      [:span.license-term (license-term-text term)]]]))

(defn- choose-license
  [licenses desired-license]
  [:div#license-section.validation-group
   (map (partial license-radio desired-license) (sort-by :license/term licenses))])

;; =============================================================================
;; Pets

(defn- application-exists?
  [application]
  (not (nil? (:db/id application))))

(defn- has-pet?
  [{:keys [member-application/pet] :as application}]
  (and (application-exists? application) pet))

(defn- has-dog?
  "Does applicant have a dog?"
  [{type :pet/type}]
  (= type :dog))

(defn show-when [v]
  {:style (when-not v "display: none;")})

(defn- pet-inputs [{:keys [pet/type pet/breed pet/weight db/id] :as pet}]
  [:div.row
   (when id [:input {:type "hidden" :name "pet[id]" :value id}])
   [:div.input-field.col.s4
    [:select {:id "pet-select" :name "pet[type]" :placeholder "?"}
     [:option {:value "" :disabled true :selected (nil? pet)} "Select Type"]
     [:option {:value "dog" :selected (= type :dog)} "Dog"]
     [:option {:value "cat" :selected (= type :cat)} "Cat"]]
    [:label {:for "pet-select"} "Type of Pet"]]
   [:div.input-field.dog-field.col.s4 (show-when (has-dog? pet))
    [:input {:id "breed" :type "text" :name "pet[breed]" :value breed}]
    [:label {:for "breed"} "Breed"]]
   [:div.input-field.dog-field.col.s4 (show-when (has-dog? pet))
    [:input {:id "weight" :type "number" :name "pet[weight]" :value weight}]
    [:label {:for "weight"} "Weight (lbs)"]]])

(defn- choose-pet
  [{:keys [member-application/pet] :as application}]
  (let [has-no-pet (and (application-exists? application) (nil? pet))]
    (list
     [:div#pets-section.validation-group
      [:p
       [:input {:type     "radio"
                :name     "has-pet"
                :id       "pets-radio-yes"
                :value    "yes"
                :required true
                :data-msg "Please choose 'Yes' or 'No'."
                :checked  (has-pet? application)}]
       [:label {:for "pets-radio-yes"} "Yes"]]
      [:p
       [:input {:type    "radio"
                :name    "has-pet"
                :id      "pets-radio-no"
                :value   "no"
                :checked has-no-pet}]
       [:label {:for "pets-radio-no"} "No"]]]
     [:div#pet-inputs (show-when (has-pet? application)) ; for jQuery
                                        ; fadeIn/fadeOut...which
                                        ; I hate. These two
                                        ; things shouldn't get
                                        ; mingled.
      (pet-inputs pet)])))

(defn- property-license-mapping
  [properties]
  (reduce
   (fn [acc {property-id :db/id
            licenses    :property/licenses
            upcoming    :property/upcoming}]
     (assoc acc property-id
            (reduce (fn [acc {license    :property-license/license
                             base-price :property-license/base-price}]
                      (assoc acc (:db/id license) (if upcoming "Price TBD" base-price)))
                    {}
                    licenses)))
   {}
   properties))

;; =============================================================================
;; API
;; =============================================================================

(defn logistics
  "Show the logistics page."
  [current-steps properties application licenses & {:keys [errors]}]
  (let [sections [["When is your ideal move-in date?"
                   (choose-availability (:member-application/desired-availability application))]
                  ["How long would you like to stay?"
                   (choose-license licenses (:member-application/desired-license application))]
                  ["Which Starcity communities are you applying to?"
                   (choose-properties properties (:member-application/desired-properties application))]
                  ["Do you have a pet?" (choose-pet application)]]]
    (common/step "Logistics" sections current-steps
                 :errors errors
                 :json [["licenseMapping" (property-license-mapping properties)]])))
