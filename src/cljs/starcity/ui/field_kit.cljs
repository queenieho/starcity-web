(ns starcity.ui.field-kit
  (:require [cljsjs.field-kit]
            [starcity.util :refer [log]]))

;; =============================================================================
;; Helpers

(defn- by-id
  [element-id]
  (.getElementById js/document element-id))

(defn- registered?
  [registry element-id]
  (get @registry element-id))

(defn- fk-text-field [element formatter did-change]
  (let [tf             (js/FieldKit.TextField. element formatter)
        delegate-attrs {:textDidChange #(did-change [(.value %) (.text %)])}]
    (.setDelegate tf (clj->js delegate-attrs))
    tf))

(defn- install!* [registry element-id formatter did-change]
  (let [elt (by-id element-id)]
    (swap! registry assoc element-id (fk-text-field elt (formatter) did-change))))

;; =============================================================================
;; API

(defn ssn-formatter []
  (js/FieldKit.SocialSecurityNumberFormatter.))

(defn phone-formatter []
  (js/FieldKit.PhoneFormatter.))

(defn make-registry []
  (atom {}))

(defn install!
  ([registry element-id formatter]
   (install! registry element-id formatter identity))
  ([registry element-id formatter did-change]
   (when-not (registered? registry element-id)
     (install!* registry element-id formatter did-change))))
