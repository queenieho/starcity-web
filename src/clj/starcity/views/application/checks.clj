(ns starcity.views.application.checks
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
               [:div.form-group.col-sm-4
                [:label.sr-only {:for id} lbl]
                [:input.form-control {:id          id
                                      :name        (format "name[%s]" name)
                                      :placeholder lbl
                                      :value       val
                                      :required    required}]])))]
    [:div.form-group.row
     (map #(apply -name-col %) [["first" first] ["middle" middle false] ["last" last]])]))

;; =====================================
;; Address

(defn- address-section
  [{:keys [lines city state postal-code]}]
  [:div
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
                           :required    true}]]]])

;; =============================================================================
;; Personal Information

(def ^:private ymd-formatter (f/formatter :year-month-day))

(defn- birthday-section
  [dob]
  [:div
   [:div.row
    (let [max-dob (f/unparse ymd-formatter (t/minus (t/now) (t/years 18)))]
      [:div.form-group.col-lg-12
       [:input.form-control {:id       "dob"
                             :name     "dob"
                             :type     "date"
                             :max      max-dob
                             :value    dob
                             :required true}]])]])

;; =============================================================================
;; Plaid

(defn- plaid-section
  [plaid-id]
  [:div#plaid-section
   (if plaid-id
     [:button#link-button.btn.btn-lg.btn-success.disabled {:type "button"}
      [:span.glyphicon.glyphicon-ok "&nbsp;"] "Thanks!"]
     [:button#link-button.btn.btn-lg.btn-info {:type "button"}
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
(s/def ::postal-code (partial re-matches #"^\d{5}(-\d{4})?$"))
(s/def ::address (s/keys :opt-un [::lines ::city ::state ::postal-code]))

(defn checks
  "Render the checks page."
  [current-steps {:keys [name address dob plaid-id]} & {:keys [errors]}]
  (let [sections [["What is your full legal name?"
                   (name-section name)]
                  ["When were you born?"
                   (birthday-section dob)]
                  ["Where do you currently live?"
                   (address-section address)]
                  ["Finally, let's verify your income."
                   (plaid-section plaid-id)]]]
    (common/application
     current-steps
     [:div.question-container
      (common/error-alerts errors)
      [:form {:method "POST"}
       [:ul.question-list
        (for [[title content] sections]
          (common/section title content))]
       common/onward]]
     :title "Personal Information"
     :json [["plaid" {:key      (get-in config [:plaid :public-key])
                      :env      (get-in config [:plaid :env])
                      :complete (not (nil? plaid-id))}]]
     :js ["https://cdn.plaid.com/link/stable/link-initialize.js"
          "bower/jquery-validation/dist/jquery.validate.js"
          "validation-defaults.js"
          "checks.js"])))

(s/fdef checks
        :args (s/cat :current-steps :starcity.models.application/steps
                     :form-data (s/keys :req-un [::name ::address]
                                        :opt-un [::dob ::plaid-id])
                     :opts      (s/keys* :opt-un [::errors]))
        :ret  string?)
