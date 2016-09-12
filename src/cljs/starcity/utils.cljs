(ns starcity.utils
  (:import [goog.ui IdGenerator]) )

(defn event-value [event]
  (.. event -target -value))

(defn by-class [class-name]
  (array-seq (.getElementsByClassName js/document class-name)))

(defn guid []
  (.getNextUniqueId (.getInstance IdGenerator)))
