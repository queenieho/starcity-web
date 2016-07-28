(ns starcity.datomic.migrations
  (:require [starcity.environment :refer [environment]]))

(def ^:private prefix-key
  (partial keyword "starcity"))

(defn- parse-requires [requires]
  (mapv #(prefix-key (name %)) requires))

;; TODO: lotta repetition...
(defmacro defnorms
  [norm-name & body]
  (let [maybe-args (first body)]
    (if (vector? maybe-args)
      (let [m# (apply hash-map (rest body))]
        `(let [norms# {~(prefix-key (name norm-name))
                       {:txes     [(fn ~maybe-args
                                     ~(:txes m#))]
                        :requires ~(parse-requires (:requires m#))
                        :env      ~(:env m#)}}]
           (def ~norm-name norms#)))
      (let [m# (apply hash-map body)]
        `(let [norms# {~(prefix-key (name norm-name))
                       {:txes     [~(:txes m#)]
                        :requires ~(parse-requires (:requires m#))
                        :env      ~(:env m#)}}]
           (def ~norm-name norms#))))))

(defn parse-norms
  "A norm-map may contain an :env keyword, which specifies the environment under
  which this migration should be run. The :env can take the form of a set of
  environments, or a single environment. This function includes the "
  [norm]
  (letfn [(-parse-env [without-norm [k {:keys [env] :as norm-map}]]
            (let [with-norm (assoc without-norm k norm-map)]
              (cond
                (nil? env) with-norm
                (set? env) (if (env environment) with-norm without-norm)
                :otherwise (if (= env environment) with-norm without-norm))))]
    (reduce -parse-env {} norm)))

(defn import-norm [sym]
  `(require '[~(symbol (format "%s.%s" (name (ns-name *ns*)) (name sym))) :as ~sym]))

(defmacro defmigration [migration-name & syms]
  `(do
     ~@(map import-norm syms)
     (defn ~migration-name []
       (parse-norms (merge ~@(map #(symbol (format "%s/%s" (name %) (name %))) syms))))))
