(ns starcity.utils
  (:import [goog.ui IdGenerator]) )

(defn event-value [event]
  (.. event -target -value))

(defn by-class [class-name]
  (array-seq (.getElementsByClassName js/document class-name)))

(defn guid []
  (.getNextUniqueId (.getInstance IdGenerator)))

(defn remove-at
  "Remove element at index `i` from vector `v`."
  [v i]
  (vec (concat (subvec v 0 i) (subvec v (inc i)))))
