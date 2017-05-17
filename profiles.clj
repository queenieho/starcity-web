{:dev {:source-paths ["src/clj" "src/cljs" "env/dev"]
       :plugins      [[lein-figwheel "0.5.8" :exclusions [org.clojure/clojure org.clojure/core.async]]
                      [lein-cooper "1.2.2" :exclusions [org.clojure/clojure]]]
       :dependencies [[figwheel-sidecar "0.5.8"]
                      [binaryage/devtools "0.9.2"]]
       :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}

 :uberjar {:aot          :all
           :main         starcity.core
           :source-paths ["src/clj" "src/cljs"]
           :prep-tasks   ["compile" ["cljsbuild" "once"]]
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
                     {:id           "apply"
                      :source-paths ["src/cljs/apply" "src/cljs/starcity"]
                      :jar          true
                      :compiler     {:main             apply.core
                                     :optimizations    :advanced
                                     :elide-asserts    true
                                     :pretty-print     false
                                     :parallel-build   true
                                     :asset-path       "/js/cljs/apply/out"
                                     :output-dir       "resources/public/js/cljs/apply/out"
                                     :output-to        "resources/public/js/cljs/apply.js"
                                     :externs          ["externs/stripe.ext.js"
                                                        "externs/chatlio.ext.js"]
                                     :closure-warnings {:externs-validation :off
                                                        :non-standard-jsdoc :off}}}
                     {:id           "mars"
                      :source-paths ["src/cljs/mars" "src/cljs/starcity"]
                      :jar          true
                      :compiler     {:main             mars.core
                                     :optimizations    :advanced
                                     :elide-asserts    true
                                     :pretty-print     false
                                     :parallel-build   true
                                     :asset-path       "/js/cljs/mars/out"
                                     :output-dir       "resources/public/js/cljs/mars/out"
                                     :output-to        "resources/public/js/cljs/mars.js"
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
