(ns user
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.spec.test :as stest]
            [figwheel-sidecar.repl-api :as ra]
            [figwheel-sidecar.system :refer [fetch-config]]
            [starcity.server]
            [starcity.seed]
            [starcity.datomic]
            [starcity.log]
            [starcity.nrepl]
            [starcity.config]
            [starcity.environment]
            [starcity.services.mailchimp]
            [starcity.services.mailgun]
            [starcity.scheduler]
            [starcity.events.observers]
            [starcity.countries]
            ;; convenience
            [starcity.datomic :refer [conn]]
            [taoensso.timbre :as timbre]
            [mount.core :as mount :refer [defstate]]))

(stest/instrument)

(timbre/refer-timbre)

;; =============================================================================
;; Figwheel

(defn- start-figwheel []
  (when-not (ra/figwheel-running?)
    (timbre/debug "starting figwheel server...")
    (ra/start-figwheel!)))

(defstate ^{:on-reload :noop} figwheel
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

;; =============================================================================
;; CLJS Repls

(defn mars-repl []
  (ra/cljs-repl "mars"))

(defn admin-repl []
  (ra/cljs-repl "admin"))

(defn apply-repl []
  (ra/cljs-repl "apply"))
