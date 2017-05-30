(ns starcity.services.community-safety
  (:require [cheshire.core :as json]
            [clj-time
             [coerce :as c]
             [core :as t]]
            [clojure
             [spec :as s]
             [string :as string]]
            [org.httpkit.client :as http]
            [plumbing.core :as plumbing]
            [starcity.config.community-safety :as config]))

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
;; Actions
;; =============================================================================

(s/def ::non-empty-string (s/and string? (comp not string/blank?)))
(s/def ::middle-name ::non-empty-string)
(s/def ::city ::non-empty-string)
(s/def ::state ::non-empty-string)
(s/def ::postal-code ::non-empty-string)
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
                                             (plumbing/assoc-when
                                              :MiddleName middle-name
                                              :City city
                                              :State state
                                              :ZipCode postal-code))))))

(s/fdef background-check
        :args (s/cat :account-id integer?
                     :first-name ::non-empty-string
                     :last-name ::non-empty-string
                     :email ::non-empty-string
                     :dob inst?
                     :opts ::background-check-opts))

;; =============================================================================
;; Selectors
;; =============================================================================

(defn report-url
  "The URL of the background check report."
  [background-check-response]
  (get-in background-check-response [:headers :location]))
