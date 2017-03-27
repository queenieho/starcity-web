(ns starcity.services.weebly
  (:require [clojure.core.async :refer [chan put! close!]]
            [org.httpkit.client :as http]
            [cheshire.core :as json]
            [starcity.config.weebly :as config]
            [starcity.environment :as env]))

(defn- cb [c]
  (fn [{body :body}]
    (let [body (json/parse-string body true)]
      (if-let [e (:error body)]
        (put! c (ex-info "Error in request!" e))
        (put! c body))
      (close! c))))

(defn add-subscriber!
  "Add `email` to our newsletter using the Weebly Promote API.
  NOTE: This is NOT a public api, and is likely to break at some point."
  [email]
  (let [c (chan 1)]
    (if (env/is-production?)
      (http/post (format "https://promote.weebly.com/site/%s/leadForm/%s/lead"
                         config/site-id config/form-id)
                 {:headers {"Accept"       "application/json"
                            "Content-Type" "application/json"}
                  :body    (json/encode {:email   email
                                         :form_id config/form-id
                                         :optIn   false
                                         :site_id config/site-id})}
                 (cb c))
      ;; It's not production, so succeed immediately
      (put! c {:message "TEST"}))
    c))

(comment
  @(http/post "https://promote.weebly.com/site/877529219857838194/leadForm/e003b5de-6590-4086-a278-b6fdc7dcf822/lead"
              {:headers {"Accept"       "application/json"
                         "Content-Type" "application/json"}
               :body    (json/encode {:email      "test2@test.com"
                                      :first_name "Test"
                                      :last_name  "User"
                                      :form_id    "e003b5de-6590-4086-a278-b6fdc7dcf822"
                                      :optIn      false
                                      :site_id    "877529219857838194"})})

  )
