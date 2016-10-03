(ns starcity.api.apply
  (:require [starcity.models
             [account :as account]
             [apply :as apply]
             [property :as property]
             [license :as license]]
            [starcity.api.common :as api]
            [starcity.utils.validation :refer [valid? errors-from]]
            [starcity.states :as states]
            [compojure.core :refer [context defroutes GET POST]]
            [taoensso.timbre :as timbre]
            [bouncer.core :as b]
            [bouncer.validators :as v :refer [defvalidator]]
            [clj-time.core :as t]
            [clj-time.coerce :as c]))

(timbre/refer-timbre)

;; =============================================================================
;; Initialize
;; =============================================================================

(defn initialize-handler
  "Produce the requesting user's application progress."
  [req]
  (let [account-id (api/account-id req)]
    (api/ok (merge (apply/progress account-id)
                   (apply/initial-data)))))

;; =============================================================================
;; Update
;; =============================================================================

;; =============================================================================
;; Helpers

(defmulti ^:private validate (fn [_ k] k))

(defvalidator members
  {:default-message-format "Invalid community selection(s)."}
  [values coll]
  (every? #(coll %) values))

(defmethod validate :logistics/communities
  [data _]
  (let [internal-names (->> (property/many [:property/internal-name])
                            (map :property/internal-name))]
    (b/validate
     data
     {:communities [[v/required :message "You must choose at least one community."]
                    [members (set internal-names)]]})))

(defmethod validate :logistics/license
  [data _]
  (let [valid-ids (->> (license/licenses) (map :db/id))]
    (b/validate
     data
     {:license [[v/required :message "You must choose a license."]
                [v/member (set valid-ids) :message "The chosen license is invalid."]]})))

(defmethod validate :logistics/move-in-date
  [data _]
  (b/validate
   data
   {:move-in-date [[v/required :message "You must supply a move-in-date."]
                   v/datetime]}))

(defmethod validate :logistics/pets
  [data _]
  (let [has-pet? :has-pet
        has-dog? (comp #{"dog"} :pet-type)]
    (b/validate
     data
     {:has-pet  [[v/required :message "Please let us know whether or not you have a pet."]
                 v/boolean]
      :pet-type [[v/required :message "Please let us know what type of pet you have." :pre has-pet?]
                 [v/member #{"cat" "dog"} :message "Your pet must be either a cat or a dog." :pre has-pet?]]
      :breed    [[v/required :message "Please let us know what kind of dog you have." :pre has-dog?]]
      :weight   [[v/required :message "Please let us know how much your dog weights." :pre has-dog?]
                 [v/integer :message "The weight must be an integer."]]})))

(defmethod validate :personal/phone-number
  [data _]
  (b/validate
   data
   {:phone-number [[v/required :message "You must supply a phone number."]
                   [v/matches #"^1?\(?\d{3}\)?(\s+)?\d{3}\-?\d{4}$" :message "The supplied phone number is invalid."]]}))

(defvalidator over-eighteen?
  {:default-message-format "You must be at least 18 years old."}
  [date]
  (t/before? (c/to-date-time date) (t/minus (t/now) (t/years 18))))

(defmethod validate :personal/background
  [data _]
  (b/validate
   data
   {:consent [[v/boolean :message ":consent must be a boolean."] v/required]
    :dob     [[v/required :message "Your date-of-birth is required."] v/datetime over-eighteen?]
    :name    {:first [[v/required :message "Your first name is required."]]
              :last  [[v/required :message "Your last name is required."]]}
    :address {:state [[v/required :message "The state that you presently live in is required."]
                      [v/member states/abbreviations :message "Please supply a valid state."]]
              :city  [[v/required :message "The city that you live in is required."]]
              :zip   [[v/required :message "Your postal code is required."]
                      [v/matches #"^\d{5}(-\d{4})?$" :message "Please enter a valid US postal code."]]}}))

(defmethod validate :community/why-starcity
  [data _]
  (b/validate
   data
   {:why-starcity [[v/required :message "Please tell us about why you want to join Starcity."]]}))

(defmethod validate :community/about-you
  [data _]
  (b/validate
   data
   {:free-time [[v/required :message "Please tell us about what you like to do in your free time."]]}))

(defmethod validate :community/communal-living
  [data _]
  (b/validate
   data
   {:prior-experience [[v/required :message "Please tell us about your experiences with communal living."]]
    :skills           [[v/required :message "Please tell us about your skills."]]}))

;; =============================================================================
;; Handlers

(def ^:private path->key
  (partial apply keyword))

(defn update-handler
  "Handle an update of user's application."
  [{:keys [params] :as req}]
  (let [account-id (api/account-id req)
        path       (path->key (:path params))
        vresult    (validate (:data params) path)]
    (if-not (valid? vresult)
      (api/malformed {:errors (errors-from vresult)})
      (try
        (apply/update (:data params) account-id path)
        (api/ok (apply/progress account-id))
        (catch Exception e
          (error e "Error encountered during update!")
          (api/server-error))))))

(defn- save-files
  [account-id file-or-files]
  (let [files (if (map? file-or-files) [file-or-files] file-or-files)]
    (account/save-income-files! account-id files)))

(defn income-files-handler
  [{:keys [params] :as req}]
  (let [account-id (api/account-id req)]
    (if-let [file-or-files (:files params)]
      (try
        (let [files (if (map? file-or-files) [file-or-files] file-or-files)
              paths (account/save-income-files! account-id files)]
          (api/ok (apply/progress account-id)))
        (catch Exception e
          (api/server-error "Something went wrong while uploading your proof of income. Please try again.")))
      (api/malformed {:errors ["You must choose at least one file to upload."]}))))

(defn help-handler
  [{:keys [params] :as req}]
  (api/ok {:message "Success!"}))

;; =============================================================================
;; Routes
;; =============================================================================

;; /api/v1/apply/...
(defroutes routes

  (GET "/" [] initialize-handler)

  (POST "/update" [] update-handler)

  (POST "/verify-income" [] income-files-handler)

  (POST "/help" [] help-handler)
  )

(comment

  {:account           {:name {:first "Josh" :middle "Adam" :last "Lehman"}}
   :communities       ["2072mission"]
   :license           6
   :move-in-date      nil ; #inst "..."
   :phone-number      "5103817881"
   :background-check  {:consent true
                       :dob     "inst"
                       :city    "Oakland"
                       :state   "CA"
                       :zip     "94611"}
   :income            {:verified true}
   :community-fitness {:why-interested          ""
                       :free-time               ""
                       :dealbreakers            ""
                       :prior-community-housing ""
                       :skills                  ""}}

  )
