(ns starcity.pages.application.logistics
  (:require [starcity.datomic :refer [conn]]
            [starcity.datomic.util :refer :all]
            [starcity.pages.base :refer [base]]
            [starcity.pages.util :refer :all]
            [starcity.models.property :as p]
            [starcity.models.application :as application]
            [datomic.api :as d]
            [clj-time.coerce :as c]
            [clj-time.format :as f]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [ring.util.response :as response]
            [clojure.string :refer [lower-case trim]]
            [starcity.util :refer [str->int transform-when-key-exists]]
            [clojure.spec :as s]))

;; TODO:
;; 1. Better date formatting DONE
;; 2. Sorted dates, ascending DONE
;; 3. Completion button appears/enables when requisite fields are filled out DONE
;; 4. Submit structured data to server
;; 5. Update rental application entity

;; =============================================================================
;; Helpers
;; =============================================================================

;; =============================================================================
;; General

(def ^:private view-formatter (f/formatter "MMMM d, y"))
(def ^:private value-formatter (f/formatter :basic-date))

(defn- parse-date
  ([date-time]
   (parse-date date-time view-formatter))
  ([date-time formatter]
   (f/unparse formatter (c/from-date date-time))))

;; =============================================================================
;; save!

(defn- property-exists? [property-id]
  (:property/name (one (d/db conn) property-id)))

(defn- validate-params
  "Validate that the submitted logistics parameters are valid."
  [params]
  (letfn [(has-pet? [m] (= "yes" (:has-pet m)))
          (has-dog? [m] (and (has-pet? m) (= (get-in m [:pet :type]) "dog")))]
    (b/validate
     params
     {:availability               [(required "Availability must be indicated.")]
      :property-id                [(required "A property must be selected.")
                                   [property-exists? :message "That property is invalid."]]
      :selected-lease             [(required "A lease must be selected")]
      :has-pet                    [(required "A 'yes' or 'no' must be provided.")]
      :num-residents-acknowledged [(required "The per-unit resident limit must be acknowledged.")]
      [:pet :type]                [[v/required :message "A type of pet must be selected." :pre has-pet?]
                                   [v/member #{:dog :cat} :message "Only dogs and cats are allowed." :pre has-pet?]]
      [:pet :breed]               [[v/required :message "You must select a breed for your dog." :pre has-dog?]]
      [:pet :weight]              [[v/required :message "You must select a weight for your dog." :pre has-dog?]]})))

(defn- clean-params
  "If the params are truthy, transform their values to match model spec."
  [params]
  (when params
    (transform-when-key-exists params
      {:selected-lease str->int
       :property-id    str->int
       :pet            {:weight str->int
                        :type   (comp keyword trim lower-case)
                        :breed  (comp trim lower-case)}})))

(defn- get-availability
  [property-id simple-dates]
  (let [property (one (d/db conn) property-id)
        avail-m  (->> (p/units property)
                      (d/pull-many (d/db conn) [:unit/available-on])
                      (reduce (fn [acc {available :unit/available-on}]
                                (assoc acc (parse-date available value-formatter) available))
                              {}))]
    (map (partial get avail-m) simple-dates)))

(s/def ::get-availability
  (s/cat :property-id int?
         :simple-dates (s/spec (s/+ string?)))) ; TODO: better check than string?

(s/fdef get-availability
        :args ::get-availability
        :ret (s/* :starcity.spec/date))

;; =============================================================================
;; Components
;; =============================================================================

;; =============================================================================
;; Availability

;; TODO: Potential of no availability -- there should be an option
;; TODO: Option to select a custom move-in & messaging

(defn- availability-checkbox
  [[date rooms]]
  (let [room-text (if (> (count rooms) 1) "rooms" "room")
        val       (parse-date date value-formatter)]
    [:div.checkbox
     [:label {:for (str "availability-" val)}
      [:input {:id (str "availability-" val) :type "checkbox" :name "availability[]" :value val :required true :data-msg "You must choose at least one available move-in date."}
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
              [:select.form-control {:id "pet-select" :name "pet[type]" :placeholder "?"}
               [:option {:value "" :disabled true :selected true} "-- Select Type --"]
               [:option {:value "dog"} "Dog"]
               [:option {:value "cat"} "Cat"]]]
             [:div.dog-field.form-group.col-sm-4 {:style "display: none;"}
              [:label.sr-only {:for "breed"} "Breed"]
              [:input.form-control {:id "breed" :type "text" :name "pet[breed]" :placeholder "Breed"}]]
             [:div.dog-field.form-group.col-sm-4 {:style "display: none;"}
              [:label.sr-only {:for "weight"} "Weight (in pounds)"]
              [:input.form-control {:id "weight" :type "number" :name "pet[weight]" :placeholder "Weight (in pounds)"}]]])]
    [:div.panel.panel-primary
     [:div.panel-heading
      [:h3.panel-title "Pets"]]
     [:div#pets-panel.panel-body
      [:div.row
       [:div.col-lg-12
        [:p "Do you have a pet?"]
        [:div.form-group.radio
         [:label.radio-inline {:for "pets-radio-yes"}
          [:input {:type "radio" :name "has-pet" :id "pets-radio-yes" :value "yes" :required true}]
          "Yes"]
         [:label.radio-inline {:for "pets-radio-no"}
          [:input {:type "radio" :name "has-pet" :id "pets-radio-no" :value "no"}]
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
      [:input {:type "hidden" :name "property-id" :value (:db/id property)}]
      (choose-availability property)
      (choose-lease property)
      (choose-pets property)
      (completion property)]]))

;; =============================================================================
;; API
;; =============================================================================

(defn render [{:keys [params] :as req} & {:keys [errors] :or {errors []}}]
  ;; TODO: Render errors
  (base (page req) :js ["bower/jquery-validation/dist/jquery.validate.js"
                        "logistics.js"]))

(defn save! [{:keys [params form-params] :as req}]
  (let [vresult    (-> params clean-params validate-params)
        account-id (get-in req [:identity :db/id])]
    (if-let [{:keys [selected-lease has-pet pet availability property-id]} (valid? vresult)]
      (do
        (application/create! account-id selected-lease (get-availability property-id availability)
                             :pets (when (= has-pet "yes") [pet]))
        (response/redirect "/application"))
      (malformed (render req :errors (errors-from vresult))))))

(comment

  (validate-params
   {:availability               [""]
    :selected-lease             "123"
    :has-pet                    "yes"
    :pet                        {:type "dog" :breed "a" :weight "1"}
    :num-residents-acknowledged "yes"})

  (clean-params
   {:availability               [""]
    :selected-lease             "123"
    :has-pet                    "yes"
    :pet                        {:type "dog" :breed "a" :weight "1"}
    :num-residents-acknowledged "yes"})

  )
