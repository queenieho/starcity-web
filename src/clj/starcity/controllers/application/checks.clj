(ns starcity.controllers.application.checks
  (:require [bouncer
             [core :as b]
             [validators :as v]]
            [clj-time
             [core :as t]
             [format :as f]]
            [clojure.string :refer [join split-lines]]
            [datomic.api :as d]
            [ring.util.response :as response]
            [starcity
             [datomic :refer [conn]]
             [states :as states]
             [util :refer :all]]
            [starcity.controllers.utils :refer :all]
            [starcity.models
             [account :as account]
             [application :as application]]
            [starcity.views.application.checks :as view]
            [clj-time.coerce :as c]))

;; =============================================================================
;; Helpers
;; =============================================================================

(def ^:private ymd-formatter (f/formatter :year-month-day))

(def ^:private checks-pattern
  "The Datomic pull pattern for information required of the rental application
  in this step."
  [:account/first-name
   :account/middle-name
   :account/last-name
   :account/dob
   :account/ssn
   {:account/application
    [{:rental-application/current-address [:address/lines
                                           :address/state
                                           :address/city
                                           :address/postal-code]}
     :rental-application/income]}])

(defn- format-pull-data
  [{:keys [account/first-name account/middle-name account/last-name
           account/dob account/ssn account/application]}]
  (let [{:keys [address/lines address/state address/city address/postal-code]} (:rental-application/current-address application)]
    (remove-nil
     {:name         {:first  first-name
                     :middle middle-name
                     :last   last-name}
      :address      {:lines       (when lines (split-lines lines))
                     :state       state
                     :city        city
                     :postal-code postal-code}
      :dob          (when dob (f/unparse ymd-formatter (c/from-date dob)))
      :ssn          ssn
      :income-level (:rental-application/income application)})))

(defn- checks-data
  [account-id]
  (format-pull-data
   (d/pull (d/db conn) checks-pattern account-id)))

;; =============================================================================
;; Parameter Validation & Transforms

(defn- validate-params
  "Validate that the submitted checks parameters are valid."
  [params]
  (letfn [(-old-enough? [date]
            (let [parsed (f/parse ymd-formatter date)]
              (t/before? parsed (t/minus (t/now) (t/years 18)))))
          (-non-empty? [lines]
            (-> lines first empty? not))]
    (b/validate
     params
     {:ssn          [(required "Your social security number is required.")
                     [v/matches #"^\d{3}-\d{2}-\d{4}$" :message "Please enter a valid social security number."]]
      :dob          [(required "Your date of birth is required!")
                     [v/datetime ymd-formatter :message "Please enter a valid date of birth."]
                     [-old-enough? :message "You must be at least 18 years of age to apply."]]
      :income-level [(required "An income level must be selected.")
                     [v/member application/income-levels :message "That is not an allowable income level."]]
      :name         {:first [(required "Your first name is required.")]
                     :last  [(required "Your last name is required.")]}
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
    {:address {:lines (partial join "\n")}
     :dob     (comp c/to-sql-date (partial f/parse ymd-formatter))}))

;; =============================================================================
;; API
;; =============================================================================

(defn show-checks
  "Display the checks form with current application info."
  [{:keys [identity] :as req}]
  (let [{:keys [name address dob ssn income-level] :as data} (checks-data (:db/id identity))]
    (clojure.pprint/pprint data)
    (ok (view/checks name address ssn dob income-level))))

(defn save!
  "Save new data to the rental application."
  [{:keys [identity params] :as req}]
  (let [vresult (validate-params params)]
    (if-let [{:keys [name address ssn dob income-level] :as ps} (valid? vresult transform-params)]
      (let [application-id (:db/id (application/by-account-id (:db/id identity)))]
        (account/update! (:db/id identity) {:ssn ssn :dob dob :name name})
        (application/update! application-id {:address address :income-level income-level})
        (response/redirect "/application"))
      (let [{:keys [name address ssn dob income-level]} params]
        (malformed (view/checks name address ssn dob income-level :errors (errors-from vresult)))))))
