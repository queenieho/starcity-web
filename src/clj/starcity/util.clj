(ns starcity.util
  (:require [clojure.string :refer [trim lower-case]]))

(def clean-text (comp trim lower-case))
