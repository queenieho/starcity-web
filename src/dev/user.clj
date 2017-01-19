(ns user
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [mount.core :as mount :refer [defstate]]
            [figwheel-sidecar.repl-api :as ra]
            [figwheel-sidecar.system :refer [fetch-config]]
            [starcity.log]
            [starcity.server]
            [starcity.datomic :refer [conn]]
            [starcity.config]
            [starcity.seeder]
            [starcity.events.observers]
            [datomic.api :as d]
            [starcity.models.util :refer :all]
            [starcity.services.mailgun]
            [starcity.services.mailchimp]
            [taoensso.timbre :as timbre]
            [clojure.spec.test :as stest]))

(timbre/refer-timbre)

(stest/instrument)

;; =============================================================================
;; Figwheel

(defn- start-figwheel []
  (when-not (ra/figwheel-running?)
    (debug "Starting Figwheel server")
    (ra/start-figwheel!)))

(defstate ^{:on-reload :noop}
  figwheel
  :start (start-figwheel))

;; =============================================================================
;; Reloaded Workflow

(def start mount/start)

(def stop mount/stop)

(defn go []
  (start)
  :ready)

(defn reset []
  (stop)
  (refresh :after 'user/go))
