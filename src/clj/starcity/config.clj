(ns starcity.config
  (:require [aero.core :as aero]
            [clojure.java.io :as io]
            [mount.core :as mount :refer [defstate]]
            [toolbelt.core :as tb]))

;; =============================================================================
;; Config Loader/State
;; =============================================================================

(defstate config
  :start (-> (io/resource "config.edn")
             (aero/read-config {:resolver aero/root-resolver
                                :profile  (:env (mount/args))})))

;; =============================================================================
;; Selectors
;; =============================================================================

(defn is-development?
  [config]
  (= :dev (:env (mount/args))))

(defn is-production?
  [config]
  (= :prod (:env (mount/args))))

;; =============================================================================
;; Webserver

(defn webserver-port
  "Port to start the webserver on."
  [config]
  (tb/str->int (get-in config [:webserver :port])))

(defn session-name
  "The name of the session cookie."
  [config]
  (get-in config [:webserver :session :name]))

(defn secure-sessions?
  "Should sessions be secure?"
  [config]
  (get-in config [:webserver :session :secure]))

(defn session-domain
  "The domain for the session cookie."
  [config]
  (get-in config [:webserver :session :domain]))

;; =============================================================================
;; Datomic

(defn ^{:deprecated "1.7.0"} datomic-part
  "The Datomic partition.

  DEPRECATED: will remove after the transactor specifies our desired partition
  as its default."
  [config]
  (get-in config [:datomic :part]))

(defn datomic-uri
  "URI of the Datomic database connection."
  [config]
  (get-in config [:datomic :uri]))

;; =============================================================================
;; nrepl

(defn nrepl-port
  "Port to start the nrepl server on."
  [config]
  (tb/str->int (get-in config [:nrepl :port])))

;; =============================================================================
;; Hosts

(defn hostname
  "The hostname of this server."
  [config]
  (get-in config [:hosts :this]))

(defn apply-hostname
  "The hostname of the apply service."
  [config]
  (get-in config [:hosts :apply]))

;; =============================================================================
;; Logs

(defn log-level
  [config]
  (get-in config [:log :level]))

(defn log-appender
  "The timbre appender to use."
  [config]
  (get-in config [:log :appender]))

(defn log-file
  "The file to log to."
  [config]
  (get-in config [:log :file]))

;; =============================================================================
;; Stripe

(defn stripe-public-key
  [config]
  (get-in config [:secrets :stripe :public-key]))

(defn stripe-private-key
  [config]
  (get-in config [:secrets :stripe :secret-key]))

;; =============================================================================
;; Weebly

(defn weebly-site-id
  [config]
  (get-in config [:secrets :weebly :site-id]))

(defn weebly-form-id
  [config]
  (get-in config [:secrets :weebly :form-id]))

;; =============================================================================
;; Slack

(defn slack-client-id
  [config]
  (get-in config [:secrets :slack :client-id]))

(defn slack-secret-key
  [config]
  (get-in config [:secrets :slack :client-secret]))

(defn slack-webhook-url
  [config]
  (get-in config [:secrets :slack :webhook]))

(defn slack-username
  [config]
  (get-in config [:slack :username]))

;; =============================================================================
;; Community Safety

(defn community-safety-api-key
  [config]
  (get-in config [:secrets :community-safety :api-key]))

;; =============================================================================
;; Mailgun

(defn mailgun-domain
  [config]
  (get-in config [:mailgun :domain]))

(defn mailgun-sender
  [config]
  (get-in config [:mailgun :sender]))

(defn mailgun-api-key
  [config]
  (get-in config [:mailgun :api-key]))

;; =============================================================================
;; Plaid

(defn plaid-env
  [config]
  (get-in config [:plaid :env]))

(defn plaid-webhook-url
  [config]
  (get-in config [:plaid :webhook]))

(defn plaid-client-id
  [config]
  (get-in config [:plaid :client-id]))

(defn plaid-secret-key
  [config]
  (get-in config [:plaid :secret-key]))

(defn plaid-public-key
  [config]
  (get-in config [:plaid :public-key]))

;; =============================================================================
;; Misc

(defn file-data-dir
  "The directory to store file data."
  [config]
  (get-in config [:data-dir]))
