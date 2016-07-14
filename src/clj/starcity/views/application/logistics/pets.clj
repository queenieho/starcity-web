(ns starcity.views.application.logistics.pets)

;; =============================================================================
;; Helpers
;; =============================================================================

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
   [:div.form-group.col-sm-4
    [:label.sr-only {:for "pet-select"} "Type of Pet"]
    [:select.form-control {:id "pet-select" :name "pet[type]" :placeholder "?"}
     [:option {:value "" :disabled true :selected (nil? pet)} "-- Select Type --"]
     [:option {:value "dog" :selected (= type :dog)} "Dog"]
     [:option {:value "cat" :selected (= type :cat)} "Cat"]]]
   [:div.dog-field.form-group.col-sm-4 (show-when (has-dog? pet))
    [:label.sr-only {:for "breed"} "Breed"]
    [:input.form-control {:id "breed" :type "text" :name "pet[breed]" :placeholder "Breed" :value breed}]]
   [:div.dog-field.form-group.col-sm-4 (show-when (has-dog? pet))
    [:label.sr-only {:for "weight"} "Weight (in pounds)"]
    [:input.form-control {:id "weight" :type "number" :name "pet[weight]" :placeholder "Weight (in pounds)" :value weight}]]])

;; =============================================================================
;; API
;; =============================================================================

(defn choose-pet
  [{:keys [member-application/pet] :as application}]
  (let [has-no-pet (and (application-exists? application) (nil? pet))]
    [:div#pets-panel.form-group
     [:div.row
      [:div.col-lg-12
       [:label.control.control--radio {:for "pets-radio-yes"}
        "Yes"
        [:input {:type     "radio"
                 :name     "has-pet"
                 :id       "pets-radio-yes"
                 :value    "yes"
                 :required true
                 :data-msg "Please choose 'Yes' or 'No'."
                 :checked  (has-pet? application)}]
        [:div.control__indicator]]
       [:label.control.control--radio {:for "pets-radio-no"}
        "No"
        [:input {:type    "radio"
                 :name    "has-pet"
                 :id      "pets-radio-no"
                 :value   "no"
                 :checked has-no-pet}]
        [:div.control__indicator]]]]
     [:div#pet-inputs (show-when (has-pet? application)) ; for jQuery fadeIn/fadeOut
      (pet-inputs pet)]]))
