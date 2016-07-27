(defproject starcity "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :jvm-opts ^:replace ["-XX:MaxPermSize=128m" "-Xms512m" "-Xmx512m" "-server"]

  :dependencies [[org.clojure/clojure "1.9.0-alpha7"]
                 [org.clojure/core.async "0.2.374"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 [org.clojure/test.check "0.9.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [http-kit "2.2.0"]
                 [clj-http "2.2.0"]
                 [compojure "1.5.0"]
                 [cheshire "5.6.1"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring "1.4.0"]
                 [hiccup "1.0.5"]
                 [mount "0.1.10"]
                 [me.raynes/fs "1.4.6"]
                 [cpath-clj "0.1.2"]
                 [com.taoensso/timbre "4.3.1"]
                 [buddy "1.0.0"]
                 [bouncer "1.0.0"]
                 [nilenso/mailgun "0.2.3"]
                 [org.apache.httpcomponents/httpclient "4.5.2"]
                 [clj-time "0.12.0"]
                 ;; db
                 [com.datomic/datomic-pro "0.9.5372"]
                 [org.postgresql/postgresql "9.3-1102-jdbc41"]
                 ;; util
                 [prismatic/plumbing "0.5.3"]]

  :resource-paths ["resources" "src/clj/starcity/datomic"]

  :profiles {:dev {:source-paths ["src/dev" "src/clj"]}

             :uberjar       {:aot          [starcity.core]
                             :source-paths ["src/clj"]}}

  :repl-options {:init-ns user}

  :main starcity.core)
