(defproject starcity "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :jvm-opts ^:replace ["-XX:MaxPermSize=128m" "-Xms512m" "-Xmx512m" "-server"]

  :dependencies [[org.clojure/clojure "1.9.0-alpha3"]
                 [org.clojure/core.async "0.2.374"]
                 [org.clojure/clojurescript "1.9.36"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 ;; cljs
                 [reagent "0.5.1"]
                 [re-frame "0.7.0"]
                 [reagent-reforms "0.4.4-SNAPSHOT"]
                 [cljsjs/field-kit "2.0.4-0"]
                 [secretary "1.2.3"]
                 [venantius/accountant "0.1.7"]
                 ;; clj
                 [http-kit "2.1.19"]
                 [bidi "2.0.9"]
                 [cheshire "5.6.1"]
                 [ring/ring "1.4.0"]
                 [hiccup "1.0.5"]
                 [mount "0.1.10"]
                 [com.datomic/datomic-free "0.9.5350" :exclusions [joda-time]]
                 [me.raynes/fs "1.4.6"]
                 [cpath-clj "0.1.2"]
                 [com.taoensso/timbre "4.3.1"]
                 [buddy "0.13.0"]
                 [bouncer "1.0.0"]
                 ;; util
                 [prismatic/plumbing "0.5.3"]
                 [prismatic/schema "1.1.1"]
                 ]

  :plugins [[lein-cljsbuild "1.1.3"]]

  :profiles
  {:dev        {:source-paths ["src/dev" "src/clj" "src/cljs"]
                :plugins      [[lein-figwheel "0.5.0-2"]]
                :dependencies [[figwheel-sidecar "0.5.0-2"]
                               [binaryage/devtools "0.6.1"]]}

   :uberjar    {:aot [starcity.core]
                :prep-tasks   ["compile" ["cljsbuild" "once"]]
                :source-paths ["src/clj" "src/cljs"]
                :cljsbuild
                {:builds {:main
                          {:source-paths ["src/cljs"]
                           :jar          true
                           :compiler     {:optimizations    :advanced
                                          :elide-asserts    true
                                          :pretty-print     false
                                          :externs          []
                                          :output-dir       "resources/public/js/app/out"
                                          :output-to        "resources/public/js/app/main.js"
                                          :closure-warnings {:externs-validation :off
                                                             :non-standard-jsdoc :off}}}}}}

   }

  :repl-options {:init-ns          user
                 :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  :clean-targets ^{:protect false} ["resources/public/js/app"
                                    :target-path]

  :main starcity.core)
