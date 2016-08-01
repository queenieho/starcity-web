(ns starcity.services.mailchimp
  (:require [mount.core :as mount :refer [defstate]]
            [starcity.config :refer [config]]
            [starcity.environment :refer [environment]]
            [org.httpkit.client :as http]
            [cheshire.core :as json]))

(defn- mailchimp-endpoint [data-center]
  (format "https://%s.api.mailchimp.com/3.0" data-center))

(defn- create-request
  [{:keys [username api-key data-center] :as config} method endpoint]
  (assert (and username api-key data-center)
          "A mailchimp username, api-key and server are required!")
  (let [base-endpoint (mailchimp-endpoint data-center)
        url           (str base-endpoint endpoint)]
    (fn [opts cb]
      (http/request
       (merge {:method     method
               :url        url
               :basic-auth [username api-key]}
              opts)
       cb))))

(defn- add-subscriber-request
  [{:keys [lists] :as config}]
  (let [endpoint (format "/lists/%s/members" (:subscribers lists))]
    (fn subscriber-request
      ([email-address]
       (subscriber-request email-address "subscribed" identity))
      ([email-address status-or-cb]
       (if (fn? status-or-cb)
         (subscriber-request email-address "subscribed" status-or-cb)
         (subscriber-request email-address status-or-cb identity)))
      ([email-address status cb]
       (let [request (create-request config :post endpoint)
             body    (json/generate-string {:email_address email-address
                                            :status        status})]
         (request {:body    body
                   :headers {"Content-Type" "application/json"}}
                  cb))))))

(defstate add-interested-subscriber!
  :start (add-subscriber-request (:mailchimp config)))
