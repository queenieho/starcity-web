(ns starcity.config
  (:require [aero.core :refer [read-config]]))

(defn config [profile]
  (assert (#{:production :development} profile)
          (format "Profile must be one of #{:production :development}, not %s!" profile))
  (-> (read-config "resources/config/config.edn" {:profile profile})
      (assoc :profile profile)))
