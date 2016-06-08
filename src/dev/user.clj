(ns user
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [mount.core :as mount :refer [defstate]]
            [figwheel-sidecar.repl-api :as ra]
            [figwheel-sidecar.system :refer [fetch-config]]
            [starcity.logger]
            [starcity.server]
            [starcity.datomic]
            [starcity.config]
            [starcity.services.mailchimp]
            [taoensso.timbre :as timbre]
            [schema.core :as s]))

(timbre/refer-timbre)

(s/set-fn-validation! true) ; turn on validation globally during development

;; =============================================================================
;; Figwheel

(defn- start-figwheel [config]
  (when-not (ra/figwheel-running?)
    (debug "Starting Figwheel server")
    (ra/start-figwheel! config)))

(defstate ^{:on-reload :noop}
  figwheel
  :start (start-figwheel (fetch-config)))

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

(defn cljs-repl []
  (ra/cljs-repl))
