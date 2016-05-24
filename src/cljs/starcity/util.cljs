(ns starcity.util
  (:require [cljs.reader :as reader])
  (:import [goog.ui IdGenerator]))

(defn guid []
  (.getNextUniqueId (.getInstance IdGenerator)))

(defn event-value [event]
  (.. event -target -value))

(defn log [& args]
  (.apply js/console.log js/console (to-array args)))

(defn warn [& args]
  (.apply js/console.warn js/console (to-array args)))

(defn error [& args]
  (.apply js/console.error js/console (to-array args)))

(defn by-class [class-name]
  (array-seq (.getElementsByClassName js/document class-name)))
