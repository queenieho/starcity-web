(ns starcity.test.datomic
  (:require [datomic.api :as d]
            [starcity-db.core :as starcity-db]))

;; =============================================================================
;; Connection Fixture
;; =============================================================================

(def ^:dynamic *conn* nil)

(defn acquire-conn []
  (let [db-name (gensym)
        db-uri  (str "datomic:mem://" db-name)]
    (d/create-database db-uri)
    (let [conn (d/connect db-uri)]
      (starcity-db/conform-schema conn)
      conn)))

(defn release-conn [conn]
  (d/release conn))

(defmacro with-conn
  "Acquires a datomic connection and binds it locally to symbol while executing
  body. Ensures resource is released after body completes. If called in a
  dynamic context in which *resource* is already bound, reuses the existing
  resource and does not release it."
  [symbol & body]
  `(let [~symbol (or *conn* (acquire-conn))]
     (try ~@body
          (finally
            (when-not *conn*
              (release-conn ~symbol))))))

(defn conn-fixture
  "Fixture function to acquire a Datomic connection for all tests in a
  namespace."
  [test-fn]
  (with-conn r
    (binding [*conn* r]
      (test-fn))))

(defn speculate [db tx-data]
  (:db-after (d/with db tx-data)))

;; (s/def :account/first-name
;;   #{"Josh" "Jocelyn" "Jesse" "Sarah" "Jon" "Gabriella" "Mo"})

;; (s/def :account/last-name
;;   #{"Lehman" "Robancho" "Suarez" "Auten" "Dishotsky" "Svensk" "Sakrani"})

;; (s/def :account/email
;;   (s/with-gen #(clojure.string/ends-with? % "@test.com")
;;     #(gen/fmap (fn [s] (str s "@test.com")) (gen/string-alphanumeric))))

;; (s/def ::account
;;   (s/keys :req [:account/first-name :account/last-name :account/email]))

;; (comment

;;   (gen/generate (s/gen ::account))

  ;; )
