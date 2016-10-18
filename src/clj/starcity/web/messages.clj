(ns starcity.web.messages
  (:require [starcity.controllers.utils :refer [malformed ok]])
  (:refer-clojure :exclude [key]))

;; =============================================================================
;; Internal
;; =============================================================================

(def ^:private key :starcity/messages)

;; =============================================================================
;; Helpers

(defn- is? [t]
  (comp (partial = t) :type))

(defn inject
  [req msgs wrapper]
  (let [msgs (if (sequential? msgs) (map wrapper msgs) [(wrapper msgs)])]
     (assoc req key msgs)))

(defn- from
  [pred req]
  (->> (get req key) (filter pred) (map :text)))

;; =============================================================================
;; Predicates

(def ^:private error?
  (is? :error))

(def ^:private success?
  (is? :success))
;; =============================================================================

;; Constructors

(defn- error [text]
  {:type :error :text text})

(defn- success [text]
  {:type :success :text text})

;; =============================================================================
;; API
;; =============================================================================

(defmacro respond-with-errors
  "Injects errors into the request map to make them available to the view
  function. Retrieve them with `starcity.web.errors/errors-from`."
  [req error-or-errors view-expr]
  `(-> (inject ~req ~error-or-errors ~error)
       ~view-expr
       (malformed)))

(defmacro respond-with-success
  "Injects success message(s) into the request map to make them available to the
  view function. Retrieve them with `starcity.web.errors/success-from`."
  [req msg-or-msgs view-expr]
  `(-> (inject ~req ~msg-or-msgs ~success)
       ~view-expr
       (malformed)))

(def errors-from
  "Retrieve errors from the request map."
  (partial from error?))

(def success-from
  "Retrieve success messages from the request map."
  (partial from success?))
