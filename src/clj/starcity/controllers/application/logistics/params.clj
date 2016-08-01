(ns starcity.controllers.application.logistics.params
  (:require [starcity.models.property :as property]
            [starcity.controllers.utils :refer :all]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [clojure.string :refer [lower-case trim]]
            [starcity.util :refer :all]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- has-pet? [{has-pet :has-pet}] (= "yes" has-pet))

;; =============================================================================
;; API
;; =============================================================================

(defn validate
  "Validate that the submitted logistics parameters are valid."
  [params]
  (letfn [(-has-dog? [m] (and (has-pet? m) (= (get-in m [:pet :type]) "dog")))]
    (b/validate
     params
     {:availability     [(required "Availability must be indicated.")]
      :properties       [(required "At least one property must be selected.")]
      :selected-license [(required "A license must be selected")]
      :has-pet          [(required "A 'yes' or 'no' must be provided.")]
      ;; :num-residents-acknowledged [(required "The per-unit resident limit must be acknowledged.")]
      [:pet :type]      [[v/required :message "A type of pet must be selected." :pre has-pet?]
                         [v/member #{:dog :cat} :message "Only dogs and cats are allowed." :pre has-pet?]]
      [:pet :breed]     [[v/required :message "You must select a breed for your dog." :pre -has-dog?]]
      [:pet :weight]    [[v/required :message "You must select a weight for your dog." :pre -has-dog?]]})))

(defn clean
  "Transform values in params to correct types and remove unnecessary
  information."
  [params]
  (letfn [(-clean-values [params]
            (transform-when-key-exists params
              {:selected-license str->int
               :properties       (partial map str->int)
               :pet              {:type   (comp keyword trim lower-case)
                                  :id     str->int
                                  :weight str->int
                                  :breed  (comp trim lower-case)}}))
          (-clean-pet [params]
            (if (has-pet? params)
              (-> (dissoc-when params [:pet :weight] nil?)
                  (dissoc-when [:pet :breed] empty?))
              (dissoc params :pet)))]
    (-> (-clean-values params) (-clean-pet))))
