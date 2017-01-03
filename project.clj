(defproject starcity "1.1.8"
  :description "The web app for https://joinstarcity.com"
  :url "https://joinstarcity.com"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.9.0-alpha13"]
                 [org.clojure/clojurescript "1.9.229"]
                 [org.clojure/core.async "0.2.391"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 [org.clojure/test.check "0.9.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [potemkin "0.4.3"]
                 [http-kit "2.2.0"]
                 [clj-http "3.3.0"]
                 [compojure "1.5.1"]
                 [cheshire "5.6.3"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring "1.5.0"]
                 [hiccup "1.0.5"]
                 [mount "0.1.10"]
                 [me.raynes/fs "1.4.6"]
                 [cpath-clj "0.1.2"]
                 [com.taoensso/timbre "4.7.4"]
                 [com.taoensso/nippy "2.12.2"]
                 [buddy "1.1.0"]
                 [bouncer "1.0.0"]
                 [nilenso/mailgun "0.2.3"]
                 [org.apache.httpcomponents/httpclient "4.5.2"]
                 [clj-time "0.12.0"]
                 ;; db
                 [com.datomic/datomic-pro "0.9.5372" :exclusions [com.google.guava/guava]]
                 [org.postgresql/postgresql "9.4.1211"]
                 [datomic-schema "1.3.0"]
                 ;; util
                 [prismatic/plumbing "0.5.3"]
                 ;; cljs
                 [cljsjs/flatpickr "2.0.0-rc.7-0"]
                 [cljsjs/stripe "2.0-0"]
                 [com.andrewmcveigh/cljs-time "0.4.0"]
                 [reagent "0.6.0"]
                 [re-frame "0.8.0"]
                 [secretary "1.2.3"]
                 [jalehman/accountant "0.1.8-2"]
                 [day8.re-frame/http-fx "0.1.2"]
                 [hiccups "0.3.0"]]

  :repositories {"my.datomic.com" {:url      "https://my.datomic.com/repo"
                                   :username [:env/datomic_username]
                                   :password [:env/datomic_password]}}

  :plugins [[lein-cljsbuild "1.1.4"]]

  :repl-options {:init-ns          user
                 :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  :clean-targets ^{:protect false} ["resources/public/js/cljs" :target-path]

  :cooper {"starcity" ["sass" "--watch" "resources/public/assets/stylesheets/main.scss:resources/public/assets/css/starcity.css"]}

  :main starcity.core)
