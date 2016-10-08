(ns starcity.countries
  (:require [mount.core :refer [defstate]]
            [clojure.java.io :as io]
            [cheshire.core :as json]))

(defstate countries-json :start (slurp (io/resource "countries.json")) :stop :noop)
(defstate countries :start (json/parse-string countries-json true))
(defstate codes :start (set (map :code countries)))
