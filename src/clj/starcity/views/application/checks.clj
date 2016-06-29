(ns starcity.views.application.checks
  (:require [starcity.views.base :refer [base]]
            [starcity.states :as states]
            [starcity.util :refer :all]
            [starcity.spec]
            [clojure.string :refer [capitalize]]
            [clojure.spec :as s]
            [clj-time.format :as f]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [starcity.models.application :as application]))

;; =============================================================================
;; Components
;; =============================================================================

;; =====================================
;; Name

(defn- name-section
  [{:keys [first last middle] :or {middle ""}}]
  (letfn [(-name-col
            ([name val]
             (-name-col name val true))
            ([name val required]
             (let [id  (format "%s-name" name)
                   lbl (format "%s Name" (capitalize name))]
               [:div.form-group.col-sm-4
                [:label.sr-only {:for id} lbl]
                [:input.form-control {:id          id
                                      :name        (format "name[%s]" name)
                                      :placeholder lbl
                                      :value       val
                                      :required    required}]])))]
    [:div.panel.panel-primary
     [:div.panel-heading
      [:h3.panel-title "Full Legal Name"]]
     [:div.panel-body
      [:div.row
       (map #(apply -name-col %) [["first" first] ["middle" middle false] ["last" last]])]]]))

;; =====================================
;; Address

(defn- address-section
  [{:keys [lines city state postal-code]}]
  [:div.panel.panel-primary
   [:div.panel-heading
    [:h3.panel-title "Current Address"]]
   [:div.panel-body
    [:div.form-group
     [:label {:for "address-line-1"} "Street Address"]
     [:input.form-control {:id          "address-line-1"
                           :name        "address[lines][]"
                           :placeholder "Address Line 1"
                           :value       (get lines 0)
                           :required    true}]]
    [:div.form-group
     [:label.sr-only {:for "address-line-2"} "Address"]
     [:input.form-control {:id          "address-line-2"
                           :name        "address[lines][]"
                           :placeholder "Address Line 2"
                           :value       (get lines 1)}]]
    [:div.row
     [:div.form-group.col-sm-4
      [:label {:for "city"} "City"]
      [:input.form-control {:id          "city"
                            :name        "address[city]"
                            :placeholder "City"
                            :value       city
                            :required    true}]]
     [:div.form-group.col-sm-4
      [:label {:for "state"} "State"]
      [:select.form-control {:id       "state"
                             :name     "address[state]"
                             :value    ""
                             :required true}
       [:option {:value "" :disabled true :selected (nil? state)}
        "-- Select State --"]
       (for [[abbr s] (sort-by val states/+states+)]
         [:option {:value abbr :selected (= abbr state)} s])]]
     [:div.form-group.col-sm-4
      [:label {:for "postal-code"} "Postal Code"]
      [:input.form-control {:id          "postal-code"
                            :type        "number"
                            :name        "address[postal-code]"
                            :placeholder "Postal Code"
                            :value       postal-code
                            :required    true}]]]]])

;; =============================================================================
;; Personal Information

(def ^:private ymd-formatter (f/formatter :year-month-day))

(defn- personal-section
  [ssn dob income-level]
  [:div.panel.panel-primary
   [:div.panel-heading
    [:h3.panel-title "Personal Information"]]
   [:div.panel-body
    [:div.row
     [:div.form-group.col-sm-6
      [:label {:for "ssn"} "Social Security Number"]
      [:input.form-control {:id          "ssn"
                            :name        "ssn"
                            :placeholder "123-45-6789"
                            :value       ssn
                            :required    true}]]
     (let [max-dob (f/unparse ymd-formatter (t/minus (t/now) (t/years 18)))]
       [:div.form-group.col-sm-6
        [:label {:for "dob"} "Date of Birth"]
        [:input.form-control {:id       "dob"
                              :name     "dob"
                              :type     "date"
                              :max      max-dob
                              :value    dob
                              :required true}]])]
    [:div.form-group
     [:label {:for "income-level"} "Annual Income ($)"]
     [:select.form-control {:id       "income-level"
                            :name     "income-level"
                            :required true}
      [:option {:value "" :disabled true :selected (nil? income-level)}
       "-- Select Income --"]
      (for [income application/income-levels]
        [:option {:value income :selected (= income-level income)} income])]]]])

;; =============================================================================
;; API
;; =============================================================================

(s/def ::first string?)
(s/def ::middle string?)
(s/def ::last string?)
(s/def ::name (s/keys :req-un [::first ::last] :opt-un [::middle]))
(s/def ::ssn string?)
(s/def ::dob (partial re-matches #"^\d{4}-\d{2}-\d{2}$"))
(s/def ::lines (s/+ string?))
(s/def ::city string?)
(s/def ::state :starcity.states/abbreviation)
(s/def ::postal-code (partial re-matches #"^\d{5}(-\d{4})?$"))
(s/def ::address (s/keys :opt-un [::lines ::city ::state ::postal-code]))
(s/def ::income-level (set application/income-levels))

(defn checks
  "Render the checks page."
  [{:keys [name address ssn dob income-level]} & {:keys [errors]}] ; TODO: Render errors
  (base
   [:div.container
    [:div.page-header
     [:h1 "Background &amp; Credit Checks"]]
    [:form {:method "POST"}
     (name-section name)
     (address-section address)
     (personal-section ssn dob income-level)
     [:input.btn.btn-default {:type "submit" :value "Submit"}]]]
   :js ["bower/jquery-validation/dist/jquery.validate.js"
        "bower/field-kit/public/field-kit.js"
        "validation-defaults.js"
        "checks.js"]))

(s/fdef checks
        :args (s/cat :form-data (s/keys :req-un [::name ::address]
                                        :opt-un [::ssn ::dob ::income-level])
                     :opts      (s/keys* :opt-un [::errors]))
        :ret  string?)
