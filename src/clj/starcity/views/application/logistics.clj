(ns starcity.views.application.logistics
  (:require [starcity.views.base :refer [base]]
            [starcity.datomic :refer [conn]]
            [starcity.datomic.util :refer :all]
            [starcity.models.property :as p]
            [datomic.api :as d]
            [clj-time.format :as f]
            [starcity.util :refer :all]))

;; =============================================================================
;; Helpers
;; =============================================================================

;; =====================================
;; Date Formatters

(def ^:private view-formatter (f/formatter "MMMM d, y"))
(def ^:private value-formatter (f/formatter :basic-date))

;; =============================================================================
;; Availability

;; TODO: Potential of no availability -- there should be an option
;; TODO: Option to select a custom move-in & messaging

(defn- availability-checkbox
  [chosen [date rooms]]
  (let [room-text  (if (> (count rooms) 1) "rooms" "room")
        val        (parse-date date value-formatter)
        is-chosen? #(-> (chosen %) nil? not)]
    [:div.checkbox
     [:label {:for (str "availability-" val)}
      [:input {:id (str "availability-" val) :type "checkbox" :name "availability[]" :value val :required true :data-msg "You must choose at least one available move-in date." :checked (is-chosen? date)}
       [:b (parse-date date view-formatter)]
       [:span (format " (%s %s available)" (count rooms) room-text)]]]]))

(defn- chosen-availability [application]
  (-> (d/pull
       (d/db conn)
       [:rental-application/desired-availability]
       (:db/id application))
      :rental-application/desired-availability
      set))

(defn- choose-availability
  [property application]
  (let [pattern [:db/id :unit/name :unit/description :unit/price :unit/available-on :unit/floor]
        units   (->> (p/units property)
                     (d/pull-many (d/db conn) pattern)
                     (group-by :unit/available-on)
                     (sort-by key))
        chosen  (chosen-availability application)]
    [:div.panel.panel-primary
     [:div.panel-heading
      [:h3.panel-title "Choose Availability"]]
     [:div.panel-body
      [:div.form-group
       (map (partial availability-checkbox chosen) units)]]]))

;; =============================================================================
;; Lease

(defn- lease-text
  [term price]
  (let [rem (mod term 12)]
    (cond
      (= term 1)  (format "Month-to-month - $%.0f" price)
      (even? rem) (format "%d year - $%.0f" (/ term 12) price)
      :otherwise  (format "%d month - $%.0f" term price))))

(defn- lease-option
  [application {:keys [db/id available-lease/term available-lease/price]}]
  (let [is-selected (= id (:db/id (:rental-application/desired-lease application)))]
    [:option {:value id :selected is-selected} (lease-text term price)]))

(defn- choose-lease
  [property application]
  (let [pattern [:db/id :available-lease/price :available-lease/term]
        leases  (->> (p/available-leases property)
                     (d/pull-many (d/db conn) pattern))]
    [:div.panel.panel-primary
     [:div.panel-heading
      [:h3.panel-title "Choose Lease"]]
     [:div.panel-body
      [:div.form-group
       [:label {:for "lease-select"} "Choose Lease"]
       [:select.form-control {:id "lease-select" :name "selected-lease" :required true :data-msg "Please select a lease term."}
        [:option {:value "" :disabled true :selected (nil? application)} "-- Select Lease --"]
        (map (partial lease-option application) leases)]]]]))

;; =============================================================================
;; Pets

;; TODO: Break this into smaller pieces
(defn- choose-pet
  [property application]
  (let [pet        (:rental-application/pet application)
        has-pet    (and (not (nil? application)) pet)
        has-no-pet (and (not (nil? application)) (nil? pet))]
    (letfn [(-pet-row []
              (let [has-dog (and has-pet (= (:pet/type pet) :dog))]
                [:div.row
                 (when pet
                   [:input {:type "hidden" :name "pet[id]" :value (:db/id pet)}])
                 [:div.form-group.col-sm-4
                  [:label.sr-only {:for "pet-select"} "Type of Pet"]
                  [:select.form-control {:id "pet-select" :name "pet[type]" :placeholder "?"}
                   [:option {:value "" :disabled true :selected (not has-pet)} "-- Select Type --"]
                   [:option {:value "dog" :selected (= (:pet/type pet) :dog)} "Dog"]
                   [:option {:value "cat" :selected (= (:pet/type pet) :cat)} "Cat"]]]
                 [:div.dog-field.form-group.col-sm-4 {:style (when-not has-dog "display: none;")}
                  [:label.sr-only {:for "breed"} "Breed"]
                  [:input.form-control {:id "breed" :type "text" :name "pet[breed]" :placeholder "Breed" :value (:pet/breed pet)}]]
                 [:div.dog-field.form-group.col-sm-4 {:style (when-not has-dog "display: none;")}
                  [:label.sr-only {:for "weight"} "Weight (in pounds)"]
                  [:input.form-control {:id "weight" :type "number" :name "pet[weight]" :placeholder "Weight (in pounds)" :value (:pet/weight pet)}]]]))]
      [:div.panel.panel-primary
       [:div.panel-heading
        [:h3.panel-title "Pets"]]
       [:div#pets-panel.panel-body
        [:div.row
         [:div.col-lg-12
          [:p "Do you have a pet?"]
          [:div.form-group.radio
           [:label.radio-inline {:for "pets-radio-yes"}
            [:input {:type    "radio" :name "has-pet" :id "pets-radio-yes" :value "yes" :required true
                     :checked has-pet}]
            "Yes"]
           [:label.radio-inline {:for "pets-radio-no"}
            [:input {:type "radio" :name "has-pet" :id "pets-radio-no" :value "no" :checked has-no-pet}]
            "No"]]]]
        [:div#pet-forms {:style (when (or has-no-pet (nil? application)) "display: none;")} ; for jQuery fadeIn/fadeOut
         (-pet-row)]]])))

;; =============================================================================
;; Completion

(defn- completion
  [property application]
  [:div.panel.panel-primary
   [:div.panel-heading
    [:h3.panel-title "Completion"]]
   [:div.panel-body
    [:div.form-group.checkbox
     [:label {:for "num-residents-acknowledged"}
      [:input {:id "num-residents-acknowledged" :type "checkbox" :name "num-residents-acknowledged" :required true
               :checked (not (nil? application))}
       "I acknowledge that only one resident over 18 can live in this room."]]]
    [:input.btn.btn-default {:type "submit" :value "Complete"}]]])

;; =============================================================================
;; API
;; =============================================================================

(defn logistics
  "Show the logistics page."
  [application property errors]
  ;; TODO: Do something with errors
  (base
   [:div.container
    [:div.page-header
     [:h1 "Logistics"]]
    ;; NOTE: For updates, do we want to instead want a PUT request?
    [:form {:method "POST"}
     [:input {:type "hidden" :name "property-id" :value (:db/id property)}]
     ;; TODO: Should each section get access to the entire application?
     (choose-availability property application)
     (choose-lease property application)
     (choose-pet property application)
     (completion property application)]]
   :js ["bower/jquery-validation/dist/jquery.validate.js"
        "validation-defaults.js"
        "logistics.js"]))
