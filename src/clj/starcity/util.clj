(ns starcity.util
  (:require [plumbing.core :refer [dissoc-in]]
            [potemkin :refer [import-vars]]
            [starcity.util.predicates]
            [starcity.util.date]))

(import-vars
 [starcity.util.predicates
  entity? conn? lookup?]
 [starcity.util.date
  is-first-day-of-month? end-of-day beginning-of-day])

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
  "Dissoc from `korks' when the value is falsy, or when the optionally supplied
  predicate produces a falsy value when invoked on the value."
  ([m korks]
   (dissoc-when m korks identity))
  ([m korks pred]
   (let [korks (if (sequential? korks) korks [korks])]
     (if (pred (get-in m korks))
      (dissoc-in m korks)
      m))))

(defn remove-nil
  "Recursively remove all kv pairs where the value is nil."
  [m]
  (reduce (fn [acc [k v]]
            (cond
              (map? v)   (assoc acc k (remove-nil v))
              (nil? v)   acc
              :otherwise (assoc acc k v)))
          {}
          m))

(defn find-by
  "Return the first element in `coll` matching `pred`; otherwise nil."
  [pred coll]
  (loop [x  (first coll)
         xs (rest coll)]
    (cond
      (pred x)    x
      (empty? xs) nil
      :otherwise  (recur (first xs) (rest xs)))))

(defn strip-namespaces
  "Remove all namespaces from keyword keys."
  [m]
  (reduce
   (fn [acc [k v]]
     (assoc acc (keyword (name k)) v))
   {}
   m))

(defn round
  [x & [precision]]
  (if precision
    (let [scale (Math/pow 10 precision)]
      (-> x (* scale) Math/round (/ scale)))
    (Math/round x)))
