(ns starcity.controllers.application.personal
  (:require [bouncer
             [core :as b]
             [validators :as v]]
            [clj-time
             [coerce :as c]
             [core :as t]
             [format :as f]]
            [clojure.string :refer [join split-lines]]
            [datomic.api :as d]
            [ring.util.response :as response]
            [starcity
             [datomic :refer [conn]]
             [states :as states]
             [util :refer :all]]
            [starcity.controllers.application.common :as common]
            [starcity.controllers.utils :refer :all]
            [starcity.models
             [account :as account]
             [application :as application]
             [plaid :as plaid]]
            [starcity.views.application.personal :as view]))

;; =============================================================================
;; Helpers
;; =============================================================================

(def ^:private ymd-formatter (f/formatter :year-month-day))

(def ^:private personal-pattern
  "The Datomic pull pattern for information required of the rental application
  in this step."
  [:account/first-name
   :account/middle-name
   :account/last-name
   :account/dob
   :account/phone-number
   {:account/member-application
    [{:member-application/current-address [:address/lines
                                           :address/state
                                           :address/city
                                           :address/postal-code]}]}
   :plaid/_account])

(defn- clean-data
  [{:keys [account/first-name account/middle-name account/last-name
           account/dob account/member-application plaid/_account
           account/phone-number]}]
  (let [{:keys [address/lines address/state address/city address/postal-code]} (:member-application/current-address member-application)]
    (remove-nil
     {:name         {:first  first-name
                     :middle middle-name
                     :last   last-name}
      :address      {:lines       (when lines (split-lines lines))
                     :state       state
                     :city        city
                     :postal-code postal-code}
      :dob          (when dob (f/unparse ymd-formatter (c/from-date dob)))
      :phone-number phone-number
      :plaid-id     (-> _account first :db/id)})))

(defn- personal-data
  [account-id]
  (clean-data (d/pull (d/db conn) personal-pattern account-id)))

;; =============================================================================
;; Parameter Validation & Transforms

(defn- validate-params
  "Validate that the submitted personal parameters are valid."
  [params]
  (letfn [(-old-enough? [date]
            (let [parsed (f/parse ymd-formatter date)]
              (t/before? parsed (t/minus (t/now) (t/years 18)))))
          (-non-empty? [lines]
            (-> lines first empty? not))]
    (b/validate
     params
     {:dob          [(required "Your date of birth is required!")
                     [v/datetime ymd-formatter :message "Please enter a valid date of birth."]
                     [-old-enough? :message "You must be at least 18 years of age to apply."]]
      :name         {:first [(required "Your first name is required.")]
                     :last  [(required "Your last name is required.")]}
      :phone-number [(required "Your phone number is required!")
                     [v/matches #"^\(?\d{3}\)?(\s+)?\d{3}\-?\d{4}$" :message "Please enter a valid phone number."]]
      :plaid-id     [(required "Please link your bank account so that we can verify your income.")]
      :address      {:lines       [(required "Your street address is required.")
                                   [v/min-count 1 :message "You must provide at least one address line."]
                                   [-non-empty? :message "Your street address must not be empty."]]
                     :state       [(required "The state that you presently live in is required.")
                                   [v/member states/abbreviations :message "Please supply a valid state."]]
                     :city        [(required "The city that you presently live in is required.")]
                     :postal-code [(required "Your postal code is required.")
                                   [v/matches #"^\d{5}(-\d{4})?$" :message "Please enter a valid US postal code."]]}})))

(defn- transform-params
  [params]
  (transform-when-key-exists params
    {:address  {:lines (partial join "\n")}
     :dob      (comp c/to-date (partial f/parse ymd-formatter))}))

(defn- can-view-personal?
  "Given a request, return true iff requesting user is allowed to view the
  personal page."
  [{:keys [identity] :as req}]
  (when-let [application-id (:db/id (application/by-account-id (:db/id identity)))]
    (application/logistics-complete? application-id)))

(defn- wrap-plaid-id
  [params account-id]
  (assoc params :plaid-id (:db/id (plaid/by-account-id account-id))))

;; =============================================================================
;; API
;; =============================================================================

(defn show-personal
  "Display the personal form with current application info."
  [{:keys [identity] :as req}]
  (let [data          (-> identity :db/id personal-data)
        current-steps (application/current-steps (:db/id identity))]
    (ok (view/personal current-steps data))))

(defn save!
  "Save new data to the rental application."
  [{:keys [identity params] :as req}]
  (let [account-id (:db/id identity)
        vresult    (-> params (wrap-plaid-id account-id) validate-params)]
    (if-let [{:keys [name phone-number address dob] :as ps} (valid? vresult transform-params)]
      (let [application-id (:db/id (application/by-account-id account-id))]
        (account/update! account-id {:dob dob :name name :phone-number phone-number})
        (application/update! application-id {:address address})
        (response/redirect "/application/community"))
      ;; didn't pass validation
      (let [current-steps (application/current-steps account-id)]
        (malformed (view/personal current-steps params :errors (errors-from vresult)))))))

(def restrictions
  (common/restrictions can-view-personal?))

(comment

  (let [vresult (validate-params
                 {:name {:first "Josh" :last "Lehman"},
                  :dob "1998-07-11",
                  :address
                  {:lines ["adsfa dadf" ""],
                   :city "adfa d",
                   :state "HI",
                   :postal-code "41561561"}
                  :plaid-id 1241561414})]
    (if-let [ps (valid? vresult)]
      (println "success")
      (println "no success")))

  )
