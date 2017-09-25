{:dev {:source-paths ["src/clj" "src/cljs" "env/dev"]
       :plugins      [[lein-figwheel "0.5.8" :exclusions [org.clojure/clojure org.clojure/core.async]]
                      [lein-cooper "1.2.2" :exclusions [org.clojure/clojure]]]
       :dependencies [[figwheel-sidecar "0.5.8"]
                      [binaryage/devtools "0.9.2"]
                      [com.datomic/datomic-free "0.9.5544"]
                      [com.akolov.enlive-reload "0.2.1"]
                      [clj-livereload "0.2.0"]]
       :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}

 :uberjar {:aot          :all
           :main         starcity.core
           :source-paths ["src/clj" "src/cljs"]
           :prep-tasks   ["compile" ["cljsbuild" "once"]]

           :dependencies [[com.datomic/datomic-pro "0.9.5544" :exclusions [com.google.guava/guava]]
                          [org.postgresql/postgresql "9.4.1211"]]

           :repositories {"my.datomic.com" {:url      "https://my.datomic.com/repo"
                                            :username :env/datomic_username
                                            :password :env/datomic_password}}

           :cljsbuild
           {:builds [{:id           "admin"
                      :source-paths ["src/cljs/admin" "src/cljs/starcity"]
                      :jar          true
                      :compiler     {:main             admin.core
                                     :optimizations    :advanced
                                     :elide-asserts    true
                                     :pretty-print     false
                                     :parallel-build   true
                                     :asset-path       "/js/cljs/admin/out"
                                     :output-dir       "resources/public/js/cljs/admin/out"
                                     :output-to        "resources/public/js/cljs/admin.js"
                                     :externs          ["externs/stripe.ext.js"]
                                     :closure-warnings {:externs-validation :off
                                                        :non-standard-jsdoc :off}}}

                     {:id           "onboarding"
                      :source-paths ["src/cljs/onboarding" "src/cljs/starcity"]
                      :jar          true
                      :compiler     {:main             onboarding.core
                                     :optimizations    :advanced
                                     :elide-asserts    true
                                     :pretty-print     false
                                     :parallel-build   true
                                     :asset-path       "/js/cljs/onboarding/out"
                                     :output-dir       "resources/public/js/cljs/onboarding/out"
                                     :output-to        "resources/public/js/cljs/onboarding.js"
                                     :externs          ["externs/stripe.ext.js"
                                                        "externs/chatlio.ext.js"]
                                     :closure-warnings {:externs-validation :off
                                                        :non-standard-jsdoc :off}}}]}}}
