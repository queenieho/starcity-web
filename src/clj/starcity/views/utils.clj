(ns starcity.views.utils)

;; NOTE: Not sure of a better way ATM (10/10/16) to share errors accross view &
;; controller.

(def ^:private errors-key :starcity/errors)

(defn errors-from [req]
  (get req errors-key))
