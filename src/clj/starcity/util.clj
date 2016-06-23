(ns starcity.util
  (:require [plumbing.core :refer [dissoc-in]]
            [clj-time.coerce :as c]
            [clj-time.format :as f]))

(defn transform-when-key-exists
  "(transform-when-key-exists
     {:a 1
      :b 2}
     {:a #(inc %)
      :c #(inc %)})

   => {:a 2 :b 2}"
  [source transformations]
  (reduce
   (fn [m x]
     (merge m
            (let [[key value] x
                  t (get transformations key)]
              (if (and (map? value) (map? t))
                (assoc m key (transform-when-key-exists value t))
                (if-let [transform t]
                  {key (transform value)}
                  x)))))
   {}
   source))

(defn str->int
  "Converts a string to an integer. If the input is already a number,
  returns the input. Note, if a non-integer is passed, an error will
  result."
  ([str]
   (if (number? str)
     str
     (when-not (empty? str)
       (Long. (re-find #"\d+" str)))))
  ([m & keys]
   (reduce
    (fn [m k]
      (if-let [val (k m)]
        (assoc m k (str->int val))
        m))
    m keys)))

(defn dissoc-when
  ([m korks]
   (dissoc-when m korks identity))
  ([m korks pred]
   (let [korks (if (sequential? korks) korks [korks])]
     (if (pred (get-in m korks))
      (dissoc-in m korks)
      m))))

;; TODO: spec it!
(defn parse-date
  [date-time formatter]
  (f/unparse formatter (c/from-date date-time)))
