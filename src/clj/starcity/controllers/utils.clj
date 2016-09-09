(ns starcity.controllers.utils
  (:require [ring.util.response :refer [response]]
            [bouncer.validators :as v]) )

;; =============================================================================
;; Ring Responses

(defn html-response
  [response]
  (assoc response :headers {"Content-Type" "text/html; charset=utf-8"}))

(def ok (comp html-response response))

(defn- inject-errors
  [req errors]
  `(let [errors# (if (sequential? ~errors) ~errors [~errors])]
     (assoc ~req :starcity/errors errors#)))

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

(defn required
  [message]
  [v/required :message message])

(defn errors-from
  "Extract errors from a bouncer error map."
  [[errors _]]
  (reduce (fn [acc [_ es]] (concat acc es)) [] errors))

(defn valid?
  ([vresult]
   (valid? vresult identity))
  ([[errors result] transform-after]
   (if (nil? errors)
     (transform-after result)
     false)))

(def not-valid? (comp not valid?))
