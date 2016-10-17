(ns starcity.controllers.utils
  (:require [ring.util.response :refer [response]]
            [bouncer.validators :as v]) )

;; =============================================================================
;; Ring Responses

(def ^:private errors-key :starcity/errors)

(defn html-response
  [response]
  (assoc response :headers {"Content-Type" "text/html; charset=utf-8"}))

(def ok (comp html-response response))

(defn- inject-errors
  [req errors]
  `(let [errors# (if (sequential? ~errors) ~errors [~errors])]
     (assoc ~req ~errors-key errors#)))

(defn malformed [body]
  (-> (response body)
      (html-response)
      (assoc :status 400)))

(defmacro respond-with-errors
  [req errors view-expr]
  `(-> ~(inject-errors req errors)
       ~view-expr
       (malformed)))

;; =============================================================================
;; Validation

;; TODO: This is a bad place for this.

(defn required
  [message]
  [v/required :message message])

(defn- extract-errors
  [errors-map acc]
  (reduce
   (fn [acc [k v]]
     (cond
       (sequential? v) (concat acc v)
       (map? v)        (extract-errors v acc)
       :otherwise      (throw (ex-info (str "Unexpected errors format! Expected sequential or map, got " (type v))
                                       {:offending-value v :key k}))))
   acc
   errors-map))

(defn errors-from
  "Extract errors from a bouncer error map."
  [[errors _]]
  (extract-errors errors []))

(defn valid?
  ([vresult]
   (valid? vresult identity))
  ([[errors result] transform-after]
   (if (nil? errors)
     (transform-after result)
     false)))

(def not-valid? (comp not valid?))
