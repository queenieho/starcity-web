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
             [application :as application]
             [plaid :as plaid]]
            [starcity.views.application.checks :as view]
            [starcity.views.error :as error-view]
            [starcity.util :refer [str->int]]
            [starcity.auth :refer [user-passes]]
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
   {:account/application
    [{:rental-application/current-address [:address/lines
                                           :address/state
                                           :address/city
                                           :address/postal-code]}]}
   :plaid/_account])

(defn- clean-data
  [{:keys [account/first-name account/middle-name account/last-name
           account/dob account/application plaid/_account]}]
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
      :plaid-id     (-> _account first :db/id)})))

(defn- checks-data
  [account-id]
  (clean-data (d/pull (d/db conn) checks-pattern account-id)))

(comment

  (let [acct-id (:db/id (starcity.datomic.util/one (d/db conn) :account/email "test@test.com"))]
    (checks-data acct-id))

  )

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
     {:dob      [(required "Your date of birth is required!")
                 [v/datetime ymd-formatter :message "Please enter a valid date of birth."]
                 [-old-enough? :message "You must be at least 18 years of age to apply."]]
      :name     {:first [(required "Your first name is required.")]
                 :last  [(required "Your last name is required.")]}
      :plaid-id [(required "Please link your bank account so that we can verify your income.")]
      :address  {:lines       [(required "Your street address is required.")
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
     :dob      (comp c/to-sql-date (partial f/parse ymd-formatter))}))

(defn- can-view-checks?
  "Given a request, return true iff requesting user is allowed to view the
  checks page."
  [{:keys [identity] :as req}]
  (when-let [application-id (:db/id (application/by-account-id (:db/id identity)))]
    (application/logistics-complete? application-id)))

(defn- wrap-plaid-id
  [params account-id]
  (assoc params :plaid-id (:db/id (plaid/by-account-id account-id))))

;; =============================================================================
;; API
;; =============================================================================

(defn show-checks
  "Display the checks form with current application info."
  [{:keys [identity] :as req}]
  (let [data          (-> identity :db/id checks-data)
        current-steps (application/current-steps (:db/id identity))]
    (ok (view/checks current-steps data))))

(defn save!
  "Save new data to the rental application."
  [{:keys [identity params] :as req}]
  (let [account-id (:db/id identity)
        vresult    (-> params (wrap-plaid-id account-id) validate-params)]
    (if-let [{:keys [name address dob] :as ps} (valid? vresult transform-params)]
      (let [application-id (:db/id (application/by-account-id account-id))]
        (account/update! account-id {:dob dob :name name})
        (application/update! application-id {:address address})
        (response/redirect "/application/community"))
      ;; didn't pass validation
      (let [current-steps (application/current-steps account-id)]
        (malformed (view/checks current-steps params :errors (errors-from vresult)))))))

;; TODO: Replace with multimethod?
(def restrictions
  (let [err "Please complete the <a href='/application/logistics'>logistics</a> step first."]
    {:handler  {:and [(user-passes can-view-checks?)]}
     :on-error (fn [req _]
                 (-> (error-view/error err)
                     (response/response)
                     (assoc :status 403)))}))
