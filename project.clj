(defproject starcity "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :jvm-opts ^:replace ["-XX:MaxPermSize=128m" "-Xms512m" "-Xmx512m" "-server"]

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [reagent "0.5.1"]
                 [re-frame "0.7.0"]
                 [re-com "0.8.1"]
                 [bidi "1.21.1"]
                 [cheshire "5.5.0"]
                 [ring/ring "1.4.0"]
                 [hiccup "1.0.5"]
                 [com.stuartsierra/component "0.3.1"]]

  :profiles {:dev {:source-paths ["src/dev" "src/clj" "src/cljs"]
                   :plugins [[lein-figwheel "0.5.0-2"]]
                   :dependencies [[figwheel-sidecar "0.5.0-2"]]}}

  :repl-options {:init-ns user
                 :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]})
