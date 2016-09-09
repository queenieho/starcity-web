(ns starcity.views.common
  (:require [starcity.views.base :refer [base]]
            [starcity.util :refer [find-by]]
            [plumbing.core :refer [assoc-when]]))

;; =============================================================================
;; API
;; =============================================================================

;; =============================================================================
;; Errors

(defn errors-from
  [req]
  (get req :starcity/errors))

(defn errors
  [req]
  (for [e (errors-from req)]
    [:div.alert.alert-error [:div.alert-text e]]))

;; =============================================================================
;; defpage

(defn- get-form
  [sym exprs]
  (when-let [form (find-by #(= sym (first %)) exprs)]
    (rest form)))

(defn- get-opts
  [m exprs]
  (assoc-when
   m
   :content (first (get-form 'body exprs))
   :title (first (get-form 'title exprs))
   :js (vec (get-form 'js exprs))
   :json (vec (get-form 'json exprs))))

;; TODO: Docstring
(defmacro defpage
  "Convenient syntax for defining HTML pages."
  [fn-name & exprs]
  (let [[args forms] (if (vector? (first exprs))
                       [(first exprs) (rest exprs)]
                       [[(symbol "req")] exprs])
        opts        (get-opts {:req (first args)} exprs)]
    `(defn ~fn-name ~args
       (base ~@(apply concat (seq opts))))))
