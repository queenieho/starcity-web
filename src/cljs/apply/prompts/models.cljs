(ns apply.prompts.models
  (:require [clojure.spec :as s])
  (:refer-clojure :exclude [next]))

;; =============================================================================
;; Constants
;; =============================================================================

(def ^:private prompts
  "The available prompts "
  {:overview/welcome :overview/advisor
   :overview/advisor :logistics/communities

   :logistics/communities  :logistics/license
   :logistics/license      :logistics/move-in-date
   :logistics/move-in-date :logistics/pets
   :logistics/pets         :personal/phone-number

   :personal/phone-number :personal/background
   :personal/background   :personal/income
   :personal/income       :community/why-starcity

   :community/why-starcity    :community/about-you
   :community/about-you       :community/communal-living
   :community/communal-living :overview/welcome})

(def ^:private prompts-inverted
  (reduce
   (fn [acc [k v]]
     (assoc acc v k))
   {}
   prompts))

(s/def ::prompt (set (keys prompts)))

;; =============================================================================
;; API
;; =============================================================================

(defn syncify
  "TODO: Better name."
  [m]
  (reduce
   (fn [acc [k v]]
     (assoc acc k {:local v :remote v}))
   {}
   m))

;; =============================================================================
;; Prompt navigation

(defn next
  "Given `prompt-key`, produce the next prompt."
  [prompt-key]
  (get prompts prompt-key))

(s/fdef next
        :args (s/cat :prompt-key ::prompt)
        :ret ::prompt)

(defn previous
  "Given `prompt-key`, produce the previous prompt."
  [prompt-key]
  (get prompts-inverted prompt-key))

(s/fdef previous
        :args (s/cat :prompt-key ::prompt)
        :ret ::prompt)

;; =============================================================================
;; Subscriptions

(defn complete-key
  "Given `prompt-key`, produce the key that can be subscribed to determine if this
  prompt is 'complete'."
  [prompt-key]
  (keyword (str (namespace prompt-key) "." (name prompt-key)) "complete?"))

(s/fdef complete-key
        :args (s/cat :prompt-key ::prompt)
        :ret keyword?)

(defn form-data
  "TODO: Doc"
  ([]
   (form-data identity))
  ([tf]
   (fn [{:keys [local remote] :as data} _]
     (tf (if (not= local remote) local remote)))))

(defn complete-when
  "TODO: Doc"
  ([validation-fn]
   (complete-when validation-fn identity))
  ([validation-fn tf]
   (fn [{:keys [local remote]} [_ sync-state]]
     (-> (if (= sync-state :unsynced) local remote)
         tf
         validation-fn))))
