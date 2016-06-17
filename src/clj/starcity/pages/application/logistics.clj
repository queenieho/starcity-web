(ns starcity.pages.application.logistics
  (:require [starcity.datomic :refer [conn]]
            [starcity.datomic.util :refer :all]
            [starcity.pages.base :refer [base]]
            [starcity.pages.util :refer [malformed ok]]
            [starcity.models.property :as p]
            [datomic.api :as d]
            [clj-time.coerce :as c]
            [clj-time.format :as f]))

;; TODO:
;; 1. Better date formatting DONE
;; 2. Sorted dates, ascending DONE
;; 3. Completion button appears/enables when requisite fields are filled out DONE
;; 4. Submit structured data to server
;; 5. Update rental application entity

;; =============================================================================
;; Helpers
;; =============================================================================

(def ^:private view-formatter (f/formatter "MMMM d, y"))
(def ^:private value-formatter (f/formatter :basic-date))

(defn- parse-date
  ([date-time]
   (parse-date date-time view-formatter))
  ([date-time formatter]
   (f/unparse formatter (c/from-date date-time))))

;; =============================================================================
;; Components
;; =============================================================================

;; =============================================================================
;; Availability

(defn- availability-checkbox
  [[date rooms]]
  (let [room-text (if (> (count rooms) 1) "rooms" "room")
        val       (parse-date date value-formatter)]
    [:div.checkbox
     [:label {:for (str "availability-" val)}
      [:input {:id (str "availability-" val) :type "checkbox" :name "availability" :value val :required true :data-msg "You must choose at least one available move-in date."}
       [:b (parse-date date)]
       [:span (format " (%s %s available)" (count rooms) room-text)]]]]))

(defn- choose-availability
  [property]
  (let [pattern [:db/id :unit/name :unit/description :unit/price :unit/available-on :unit/floor]
        units   (->> (p/units property)
                     (d/pull-many (d/db conn) pattern)
                     (group-by :unit/available-on)
                     (sort-by key))]
    [:div.panel.panel-primary
     [:div.panel-heading
      [:h3.panel-title "Choose Availability"]]
     [:div.panel-body
      [:div.form-group
       (map availability-checkbox units)]]]))

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
  [{:keys [db/id available-lease/term available-lease/price]}]
  [:option {:value id} (lease-text term price)])

(defn- choose-lease
  [property]
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
        [:option {:value "" :disabled true :selected true} "-- Select Lease --"]
        (map lease-option leases)]]]]))

;; =============================================================================
;; Pets

(defn- choose-pets
  [property]
  (letfn [(-pet-row []
            [:div.row
             [:div.form-group.col-sm-4
              [:label.sr-only {:for "pet-select"} "Type of Pet"]
              [:select.form-control {:id "pet-select" :name "selected-pet" :placeholder "?"}
               [:option {:value "" :disabled true :selected true} "-- Select Type --"]
               [:option {:value "dog"} "Dog"]
               [:option {:value "cat"} "Cat"]]]
             [:div.dog-field.form-group.col-sm-4 {:style "display: none;"}
              [:label.sr-only {:for "breed"} "Breed"]
              [:input.form-control {:id "breed" :type "text" :name "breed" :placeholder "Breed"}]]
             [:div.dog-field.form-group.col-sm-4 {:style "display: none;"}
              [:label.sr-only {:for "weight"} "Weight (in pounds)"]
              [:input.form-control {:id "weight" :type "number" :name "weight" :placeholder "Weight (in pounds)"}]]])]
    [:div.panel.panel-primary
     [:div.panel-heading
      [:h3.panel-title "Pets"]]
     [:div#pets-panel.panel-body
      [:div.row
       [:div.col-lg-12
        [:p "Do you have a pet?"]
        [:div.form-group.radio
         [:label.radio-inline {:for "pets-radio-yes"}
          [:input {:type "radio" :name "pets-radio" :id "pets-radio-yes" :value "yes" :required true}]
          "Yes"]
         [:label.radio-inline {:for "pets-radio-no"}
          [:input {:type "radio" :name "pets-radio" :id "pets-radio-no" :value "no"}]
          "No"]]]]
      [:div#pet-forms {:style "display: none;"} ; for jQuery fadeIn/fadeOut
       (-pet-row)]]]))

;; =============================================================================
;; Completion

(defn- completion [property]
  [:div.panel.panel-primary
   [:div.panel-heading
    [:h3.panel-title "Completion"]]
   [:div.panel-body
    [:div.form-group.checkbox
     [:label {:for "num-residents-acknowledged"}
      [:input {:id "num-residents-acknowledged" :type "checkbox" :name "num-residents-acknowledged" :required true}
       "I acknowledge that only one resident over 18 can live in this room."]]]
    [:input.btn.btn-default {:type "submit" :value "Complete"}]]])

;; =============================================================================
;; & rest

(defn- page [req]
  (let [property (one (d/db conn) :property/internal-name "alpha")]
    [:div.container
     [:div.page-header
      [:h1 "Logistics"]]
     [:form {:method "POST"}
      (choose-availability property)
      (choose-lease property)
      (choose-pets property)
      (completion property)]]))

;; =============================================================================
;; API
;; =============================================================================

(defn render [{:keys [params] :as req}]
  (base (page req) :js ["bower/jquery-validation/dist/jquery.validate.js"
                        "logistics.js"]))

(defn save! [{:keys [params] :as req}]
  (clojure.pprint/pprint params)
  (ok (render req)))
