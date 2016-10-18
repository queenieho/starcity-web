(ns starcity.services.community-safety
  (:require [starcity.config.community-safety :as config]
            [org.httpkit.client :as http]
            [cheshire.core :as json]
            [plumbing.core :refer [assoc-when]]
            [clj-time.coerce :as c]
            [clj-time.core :as t]
            [starcity.spec]
            [clojure.spec :as s]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- community-safety-request
  [endpoint params]
  (-> @(http/post (format "https://api.communitysafety.goodhire.com/v1/%s" endpoint)
                  {:body    (json/generate-string params)
                   :headers {"Content-Type"  "application/json"
                             "Authorization" (format "ApiKey %s" config/api-key)}})
      (update-in [:body] json/parse-string true)))

(s/fdef community-safety-request
        :args (s/cat :endpoint string? :params map?)
        :ret map?)

(defn- dob-params [dob]
  (let [dt (c/from-date dob)]
    {:BirthYear  (t/year dt)
     :BirthMonth (t/month dt)
     :BirthDay   (t/day dt)}))

;; =============================================================================
;; API
;; =============================================================================

(s/def ::middle-name :starcity.spec/non-empty-string)
(s/def ::city :starcity.spec/non-empty-string)
(s/def ::state :starcity.spec/non-empty-string)
(s/def ::postal-code :starcity.spec/non-empty-string)
(s/def ::address
  (s/keys :req-un [::city ::state ::postal-code]))
(s/def ::background-check-opts
  (s/keys :opt-un [::middle-name ::address]))

(defn background-check
  "Initiate a background check via the Community Safety API."
  ([account-id first-name last-name email dob]
   (background-check account-id first-name last-name email dob {}))
  ([account-id first-name last-name email dob {:keys [middle-name address]}]
   (let [{:keys [city state postal-code]} address]
     (community-safety-request "Profile" (-> {:UserID    (str account-id)
                                              :FirstName first-name
                                              :LastName  last-name
                                              :Email     email}
                                             (merge (dob-params dob))
                                             (assoc-when :MiddleName middle-name
                                                         :City city
                                                         :State state
                                                         :ZipCode postal-code))))))

(s/fdef background-check
        :args (s/cat :account-id integer?
                     :first-name :starcity.spec/non-empty-string
                     :last-name :starcity.spec/non-empty-string
                     :email :starcity.spec/non-empty-string
                     :dob :starcity.spec/date
                     :opts ::background-check-opts))
