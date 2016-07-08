(ns starcity.controllers.application.common
  (:require [ring.util.response :as response]
            [starcity.auth :refer [user-passes]]
            [starcity.views.error :as error-view]))

;; =============================================================================
;; API
;; =============================================================================

(defn restrictions
  [previous-step-name previous-step-url predicate]
  (let [err (format "Please complete the <a href='%s'>%s</a> step first."
                    previous-step-url previous-step-name)]
    {:handler  {:and [(user-passes predicate)]}
     :on-error (fn [req _]
                 (-> (error-view/error err)
                     (response/response)
                     (assoc :status 403)))}))
