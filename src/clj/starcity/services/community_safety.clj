(ns starcity.services.community-safety
  (:require [cheshire.core :as json]
            [mount.core :as mount :refer [defstate]]
            [org.httpkit.client :as http]
            [starcity.config :refer [config]]
            [clj-time.coerce :as c]
            [clj-time.core :as t]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- community-safety-request
  [endpoint {:keys [api-key]}]
  (fn [{:keys [:account/first-name
              :account/last-name
              :account/middle-name
              :account/dob
              :account/email
              :account/member-application
              :db/id]}
      cb]
    (let [dob-dt                         (c/from-date dob)
          year                           (t/year dob-dt)
          month                          (t/month dob-dt)
          day                            (t/day dob-dt)
          {:keys [:address/state
                  :address/city
                  :address/postal-code]} (:member-application/current-address
                                          member-application)]
      (http/post (format "https://api.communitysafety.goodhire.com/v1/%s" endpoint)
                 {:body    (json/generate-string
                            {:UserID     (str id)
                             :FirstName  first-name
                             :MiddleName middle-name
                             :LastName   last-name
                             :BirthYear  year
                             :BirthMonth month
                             :BirthDay   day
                             :City       city
                             :State      state
                             :ZipCode    (str postal-code)
                             :Email      email})
                  :headers {"Content-Type"  "application/json"
                            "Authorization" (format "ApiKey %s" api-key)}}
                 (fn [res]
                   (cb (update-in res [:body] json/parse-string true)))))))

(def ^:private background-check-request
  "Initiate a background check via the Community Safety API."
  (partial community-safety-request "Profile"))

;; =============================================================================
;; API
;; =============================================================================

(defstate background-check
  :start (background-check-request (:community-safety config)))

(comment

  (let [req* (community-safety-request (:community-safety config))]
    (req* {} (fn [res]
               (clojure.pprint/pprint res))))

  (let [conn    starcity.datomic/conn
        req* (background-check-request (:community-safety config))
        acct-id (:db/id (starcity.models.util/one (d/db conn) :account/email "test@test.com"))
        acct    (d/pull
                 (d/db conn)
                 [:account/first-name
                  :account/last-name
                  :account/middle-name
                  :account/dob
                  :account/email
                  :db/id
                  {:account/member-application
                   [{:member-application/current-address
                     [:address/state :address/city :address/postal-code]}]}]
                 acct-id)]
    (req*
     acct
     (fn [res]
       (clojure.pprint/pprint res))))

  )
