(ns user
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [com.stuartsierra.component :as component]
            [figwheel-sidecar.repl-api :as ra]
            [starcity.core :as sys]))

(def system nil)

(def config
  {:web-port 8080
   :db       {:uri        "datomic:mem://localhost:4334/starcity"
              :schema-dir "resources/datomic/schemas"}})

(defn init []
  (alter-var-root #'system
                  (constantly (sys/dev-system config))))

(defn start []
  (alter-var-root #'system component/start))

(defn stop []
  (alter-var-root #'system
                  (fn [s] (when s (component/stop s)))))

(defn go []
  (init)
  (start))

(defn reset []
  (stop)
  (refresh :after 'user/go))

(defn cljs-repl []
  (ra/cljs-repl))
