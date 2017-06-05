(defproject starcity "1.7.1-SNAPSHOT"
  :description "The web app for https://joinstarcity.com"
  :url "https://joinstarcity.com"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [;; Clojure
                 [org.clojure/clojure "1.9.0-alpha14"]
                 [org.clojure/clojurescript "1.9.293"]
                 [org.clojure/core.async "0.2.395"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 [org.clojure/test.check "0.9.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 ;; Web
                 [http-kit "2.2.0"]
                 [bidi "2.0.16"]        ; phasing in
                 [clj-http "3.3.0"]
                 [compojure "1.5.1"]    ; phasing out
                 [cheshire "5.6.3"]
                 [ring/ring "1.5.0"]
                 [ring-middleware-format "0.7.2"]
                 [hiccup "1.0.5"]       ; TODO: remove
                 [buddy "1.1.0"]
                 [bouncer "1.0.0"]
                 [optimus "0.19.1"]
                 [starcity/datomic-session-store "0.1.0"]
                 [starcity/customs "0.1.0"]
                 [starcity/facade "0.1.0"]
                 ;; HTTP APIs
                 [nilenso/mailgun "0.2.3"]
                 [org.apache.httpcomponents/httpclient "4.5.2"] ; dep resolution?
                 ;; Time
                 [clj-time "0.12.0"]
                 [im.chit/hara.io.scheduler "2.4.8"]
                 ;; Reloaded
                 [mount "0.1.11"]
                 ;; Datomic
                 [io.rkn/conformity "0.4.0"]
                 [starcity/blueprints "1.6.0" :exclusions [com.datomic/datomic-free]]
                 [com.datomic/datomic-pro "0.9.5544" :exclusions [com.google.guava/guava]]
                 [org.postgresql/postgresql "9.4.1211"]
                 ;; CLJS
                 [cljsjs/flatpickr "2.0.0-rc.7-0"]
                 [starcity/ant-ui "0.1.2" :exclusions [re-frame]]
                 [com.andrewmcveigh/cljs-time "0.5.0-alpha2"]
                 [reagent "0.6.0"]
                 [re-frame "0.9.2" :exclusions [reagent]]
                 [secretary "1.2.3"]    ; phasing out (bidi)
                 [venantius/accountant "0.1.7"]
                 [day8.re-frame/http-fx "0.1.2"]
                 ;; Utility
                 [aero "1.1.2"]
                 [dire "0.5.4"]
                 [potemkin "0.4.3"]
                 [me.raynes/fs "1.4.6"]
                 [cpath-clj "0.1.2"]
                 [com.taoensso/timbre "4.8.0"]
                 [com.taoensso/nippy "2.12.2"]
                 [prismatic/plumbing "0.5.3"]
                 [starcity/toolbelt "0.1.3" :exclusions [com.datomic/datomic-free]]
                 [enlive "1.1.6"]
                 [cljsjs/moment "2.17.1-0"]]

  :jvm-opts ["-server"
             "-Xmx4g"
             "-XX:+UseCompressedOops"
             "-XX:+DoEscapeAnalysis"
             "-XX:+UseConcMarkSweepGC"]

  :repositories {"my.datomic.com" {:url      "https://my.datomic.com/repo"
                                   :username :env/datomic_username
                                   :password :env/datomic_password}

                 "releases" {:url        "s3://starjars/releases"
                             :username   :env/aws_access_key
                             :passphrase :env/aws_secret_key}}

  :plugins [[lein-cljsbuild "1.1.4"]
            [s3-wagon-private "1.2.0"]]

  :jar-name "starcity-web.jar"

  :repl-options {:init-ns user}

  :clean-targets ^{:protect false} ["resources/public/js/cljs" :target-path]

  :cooper {"internal" ["sass" "--watch" "-E" "UTF-8" "style/sass/main.sass:resources/public/assets/css/starcity.css"]
           "public"   ["sass" "--watch" "-E" "UTF-8" "style/sass/public.scss:resources/public/assets/css/public.css"]
           "antd"     ["less-watch-compiler" "style/less" "resources/public/assets/css/"]}

  :main starcity.core)
