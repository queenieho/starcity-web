(ns user
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.spec.test :as stest]
            [clojure.spec :as s]
            [datomic.api :as d]
            [figwheel-sidecar.repl-api :as ra]
            [figwheel-sidecar.system :refer [fetch-config]]
            [starcity.server]
            [starcity.observers]
            [starcity.seed :as seed]
            [starcity.datomic :as datomic]
            [starcity.log]
            [starcity.nrepl]
            [starcity.config]
            [starcity.environment]
            [starcity.scheduler]
            [starcity.countries]
            [net.cgrand.reload]
            ;; convenience
            [starcity.datomic :refer [conn]]
            [taoensso.timbre :as timbre]
            [mount.core :as mount :refer [defstate]]))

(timbre/refer-timbre)

;; Autoreload on change during dev
(net.cgrand.reload/auto-reload *ns*)

;; =============================================================================
;; Figwheel

(defn start-figwheel! [& builds]
  (when-not (ra/figwheel-running?)
    (timbre/debug "starting figwheel server...")
    (apply ra/start-figwheel! builds)))

;; =============================================================================
;; Reloaded Workflow

(defn- in-memory-db?
  "There's a more robust way to do this, but it's not really necessary ATM."
  []
  (= "datomic:mem://localhost:4334/starcity"
     (:uri starcity.config.datomic/datomic)))

(defstate seed
  :start (when (in-memory-db?)
           (timbre/debug "seeding in-memory Datomic database...")
           (seed/seed datomic/conn)))

(def start mount/start)

(def stop mount/stop)

(defn go []
  (start)
  (stest/instrument)
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
