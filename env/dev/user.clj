(ns user
  (:require [clojure.tools.namespace.repl :refer [refresh refresh-all]]
            [clojure.tools.namespace.find :as nfind]
            [clojure.spec.test :as stest]
            [clojure.spec :as s]
            [datomic.api :as d]
            [figwheel-sidecar.repl-api :as ra]
            [starcity.server]
            [starcity.seed :as seed]
            [starcity.log]
            [starcity.nrepl]
            [starcity.config :as config :refer [config]]
            [starcity.countries]
            [net.cgrand.reload]
            ;; convenience
            [starcity.datomic :refer [conn]]
            [taoensso.timbre :as timbre]
            [mount.core :as mount :refer [defstate]]
            [toolbelt.core]
            [clj-livereload.server :as livereload]
            [clojure.java.io :as io]))

(timbre/refer-timbre)

;; =============================================================================
;; Figwheel

(defn start-figwheel! [& builds]
  (when-not (ra/figwheel-running?)
    (timbre/debug "starting figwheel server...")
    (apply ra/start-figwheel! builds)))

;; ============================================================================
;; Reloaded Workflow

(defn- in-memory-db?
  "There's a more robust way to do this, but it's not really necessary ATM."
  []
  (= "datomic:mem://localhost:4334/starcity"
     (config/datomic-uri config)))

(defstate seed
  :start (when (in-memory-db?)
           (timbre/debug "seeding dev database...")
           (seed/seed conn)))


(defstate livereload
  :start (livereload/start! {:paths ["resources/templates"
                                     "resources/public/assets"]
                             :debug? true})
  :stop  (livereload/stop!))

(defn autoreload []
  ;; Autoreload on change during dev
  (doseq [n (nfind/find-namespaces-in-dir (io/file "src/clj/starcity/controllers"))]
    (net.cgrand.reload/auto-reload n)))


(defn start []
  (mount/start-with-args {:env :dev}))

(def stop mount/stop)

(defn go []
  (stest/instrument)
  (start)
  (autoreload)
  :ready)

(defn go! []
  (go)
  (start-figwheel!))

(defn reset []
  (stop)
  (refresh-all :after 'user/go)
  (autoreload))

;; =============================================================================
;; CLJS Repls

(defn admin-repl []
  (ra/cljs-repl "admin"))
