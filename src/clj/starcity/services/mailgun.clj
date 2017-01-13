(ns starcity.services.mailgun
  (:refer-clojure :exclude [send])
  (:require [mailgun.mail :as mail]
            [clojure.core.async :refer [put! chan]]
            [starcity.config.mailgun :as config]
            [starcity.environment :refer [environment]]
            [mount.core :as mount :refer [defstate]]
            [org.httpkit.client :as http]
            [cheshire.core :as json]
            [taoensso.timbre :as timbre]))

(defn- send-mail-async
  "Send email to mailgun with the passed creds and the content, *but async*.

  A sample request would look like:
  (send-mail {:key \"key-3ax6xnjp29jd6fds4gc373sgvjxteol1\" :domain \"bar.com\"}
             {:from \"no-reply@bar.com\"
              :to \"someone@foo.com\"
              :subject \"Test mail\"
              :html \"Hi ,</br> How are you ?\"
              :attachment [(clojure.java.io/file \"path/to/file\")]}
             (fn [res] (println res)))"
  [{:keys [domain key] :as creds} message-content cb]
  (if (mail/validate-message message-content)
    (let [url     (mail/gen-url "/messages" domain)
          content (merge (mail/gen-auth key)
                         (mail/gen-body message-content)
                         {:keepalive 30000})]
      (http/post url content cb))
    (throw (Exception. "Invalid/Incomplete message-content"))))

;; =============================================================================
;; API
;; =============================================================================

(defn ^{:deprecated "1.2.0"} send-email
  [to subject content & {:keys [from cb] :or {from config/default-sender}}]
  (let [creds   {:key config/api-key :domain config/domain}
        payload {:from    from
                 :to      (if (= environment :production) to "josh@joinstarcity.com")
                 :subject subject
                 :html    content}]
    (if cb
      (send-mail-async creds payload (fn [res] (cb (update res :body json/parse-string true))))
      (mail/send-mail creds payload))))

(def ^{:deprecated "1.2.0"} default-signature
  [:p "Best,"
   [:br]
   [:br]
   "Mo"
   [:br]
   "Head of Community"])

;; =============================================================================
;; New Api

(def ^:private dev-to
  "josh@joinstarcity.com")

(defn- log-result
  [to from subject {:keys [error] :as res}]
  (let [res' (update res :body json/parse-string true)
        log  {:to       to
              :from     from
              :subject  subject
              :response (select-keys res' [:body :status])}]
    (if error
      (timbre/error error ::sent log)
      (timbre/trace ::sent log))
    res'))

(defn send
  "Send an email asynchronously."
  [to subject msg & {:keys [from]}]
  (let [out-c (chan 1)
        creds {:key config/api-key :domain config/domain}
        data  {:from    (or from config/default-sender)
               :to      (if (= environment :production) to "josh@joinstarcity.com")
               :subject subject
               :html    msg}]
    (send-mail-async creds data
                     (fn [res]
                       (put! out-c (log-result to from subject res))))
    out-c))
