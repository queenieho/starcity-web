(ns starcity.datomic.migrations
  (:require [starcity.environment :refer [environment]]
            [datomic.api :as d]))

(def ^:private prefix-starcity
  (partial keyword "starcity"))

(defn- parse-requires [requires]
  (mapv #(prefix-starcity (name %)) requires))

;; TODO: lotta repetition...
(defmacro defnorms
  [norm-name & body]
  (let [maybe-args (first body)]
    (if (vector? maybe-args)
      (let [m# (apply hash-map (rest body))]
        `(let [norms# {~(prefix-starcity (name norm-name))
                       {:txes     [(fn ~maybe-args
                                     ~(:txes m#))]
                        :requires ~(parse-requires (:requires m#))
                        :env      ~(:env m#)}}]
           (def ~norm-name norms#)))
      (let [m# (apply hash-map body)]
        `(let [norms# {~(prefix-starcity (name norm-name))
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

(defn- migration-init [kw]
  {:migration/init {:txes [[{:db/id                 #db/id[:db.part/db]
                             :db/ident              :migration/applied-on
                             :db/valueType          :db.type/instant
                             :db/cardinality        :db.cardinality/one
                             :db/doc                "The time at which a migration was last applied. Essentially
            used as a dummy field so that I can add the migration itself as a
            norm for future requires."
                             :db.install/_attribute :db.part/db}
                            {:db/id                 #db/id[:db.part/db]
                             :db/ident              :migration/name
                             :db/valueType          :db.type/keyword
                             :db/cardinality        :db.cardinality/one
                             :db/doc                "Name of migration being applied. See :migration/last-applied for further reference."
                             :db.install/_attribute :db.part/db}]]}
   kw              {:txes     [[{:db/id                #db/id[:db.part/user]
                                 :migration/name       kw
                                 :migration/applied-on (java.util.Date.)}]]
                    :requires [:migration/init]}})

(def ^:private prefix-migration
  (partial keyword "migration"))

(defn- parse-syms [syms]
  (if (keyword? (first syms))
    {:syms (rest (rest syms)) :requires (mapv (comp prefix-migration name) (second syms))}
    {:syms syms :requires []}))

(defn norm-merge [requires sym]
  `(-> ~(symbol (format "%s/%s" (name sym) (name sym)))
       (update-in [~(prefix-starcity (name sym)) :requires] concat ~requires)))

;; TODO: Handle the case where a map is passed directly instead of a symbol
(defmacro defmigration [migration-name & syms]
  (let [{:keys [syms requires]} (parse-syms syms)]
    `(do
       ~@(map import-norm syms)
       (defn ~migration-name []
         (parse-norms (merge ~@(map (partial norm-merge requires) syms)
                             ~(migration-init (prefix-migration (name migration-name)))))))))
