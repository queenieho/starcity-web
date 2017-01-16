(ns starcity.util.predicates)

(defn entity?
  "Is `x` a Datomic entity?"
  [x]
  (= datomic.query.EntityMap (type x)))

(defn conn?
  "Is `x` a Datomic connection?"
  [x]
  (= datomic.peer.LocalConnection (type x)))

(defn lookup?
  "is `x` a lookup ref?"
  [x]
  (and (vector? x)
       (keyword? (first x))
       (= (count x) 2)))
