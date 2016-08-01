(ns starcity.views.application.personal
  (:require [starcity.views.application.common :as common]
            [starcity.models.application :as application]
            [starcity.config :refer [config]]
            [starcity.states :as states]
            [starcity.util :refer :all]
            [starcity.spec]
            [clojure.string :refer [capitalize]]
            [clojure.spec :as s]
            [clj-time.format :as f]
            [clj-time.core :as t]
            [clj-time.coerce :as c]))

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
               [:div.input-field.col.s4
                [:input {:id       id
                         :type     "text"
                         :name     (format "name[%s]" name)
                         :value    val
                         :required required}]
                [:label {:for id} lbl]])))]
    [:div.row
     (map #(apply -name-col %) [["first" first] ["middle" middle false] ["last" last]])]))

;; =============================================================================
;; Birthdate

(def ^:private ymd-formatter (f/formatter :year-month-day))

(defn- birthday-section
  [dob]
  (let [max-dob (f/unparse ymd-formatter (t/minus (t/now) (t/years 18)))]
    [:div.input-field
     [:input#dob.datepicker {:name     "dob"
                             :type     "date"
                             :max      max-dob
                             :value    dob
                             :required true}]
     [:label {:for "dob"} "Choose Your Birthday"]]))

;; =====================================
;; Address

(defn- address-section
  [{:keys [lines city state postal-code]}]
  ;; address line 1
  (list
   [:div.row
    [:div.input-field.col.s12
     [:input {:id       "address-line-1"
              :type     "text"
              :name     "address[lines][]"
              :value    (get lines 0)
              :required true}]
     [:label {:for "address-line-1"} "Street Address"]]]

   ;; address line 2
   [:div.row
    [:div.input-field.col.s12
     [:input {:id    "address-line-2"
              :type  "text"
              :name  "address[lines][]"
              :value (get lines 1)}]
     [:label {:for "address-line-2"} "Address Line 2 (optional)"]]]

   ;; city, state, zip
   [:div.row
    ;; city
    [:div.input-field.col.s4
     [:input {:id       "city"
              :type     "text"
              :name     "address[city]"
              :value    city
              :required true}]
     [:label {:for "city"} "City"]]

    ;; state
    [:div.input-field.col.s4
     [:select {:id       "state"
               :name     "address[state]"
               :value    ""
               :required true}
      [:option {:value "" :disabled true :selected (nil? state)}
       "Select State"]
      (for [[abbr s] (sort-by val states/+states+)]
        [:option {:value abbr :selected (= abbr state)} s])]]

    ;; zip
    [:div.input-field.col.s4
     [:label {:for "postal-code"} "Postal Code"]
     [:input {:id       "postal-code"
              :type     "number"
              :name     "address[postal-code]"
              :value    postal-code
              :required true}]]]))

;; =============================================================================
;; Plaid

(defn- plaid-section
  [plaid-id]
  [:div#plaid-section.row.center
   (if plaid-id
     [:button#link-button.btn.disabled {:type "button"}
      [:i.material-icons.right "done"] "Thanks!"]
     [:button#link-button.btn.tranquil-blue.darken-1 {:type "button"}
      "Link Bank Account"])])

;; =============================================================================
;; API
;; =============================================================================

(s/def ::first string?)
(s/def ::middle string?)
(s/def ::last string?)
(s/def ::name (s/keys :req-un [::first ::last] :opt-un [::middle]))
(s/def ::dob (partial re-matches #"^\d{4}-\d{2}-\d{2}$"))
(s/def ::lines (s/+ string?))
(s/def ::city string?)
(s/def ::state :starcity.states/abbreviation)
;; NOTE: Could have been an invalid postal code!
;; (s/def ::postal-code (partial re-matches #"^\d{5}(-\d{4})?$"))
(s/def ::address (s/keys :opt-un [::lines ::city ::state ::postal-code]))

(defn personal
  "Render the personal page."
  [current-steps {:keys [name address dob plaid-id]} & {:keys [errors]}]
  (let [sections [["What is your full legal name?" (name-section name)]
                  ["When were you born?" (birthday-section dob)]
                  ["Where do you currently live?" (address-section address)]
                  ["Finally, let's verify your income." (plaid-section plaid-id)]]]
    (common/step "Personal Information" sections current-steps
                 :errors errors
                 :js   ["https://cdn.plaid.com/link/stable/link-initialize.js"]
                 :json [["plaid" {:key      (get-in config [:plaid :public-key])
                                  :env      (get-in config [:plaid :env])
                                  :complete (not (nil? plaid-id))}]])))

(s/fdef personal
        :args (s/cat :current-steps :starcity.models.application/steps
                     :form-data (s/keys :req-un [::name ::address]
                                        :opt-un [::dob ::plaid-id])
                     :opts      (s/keys* :opt-un [::errors]))
        :ret  string?)
