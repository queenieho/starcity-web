(ns starcity.views.application
  (:require [starcity.views.application.common :as common]))

;; =============================================================================
;; Helpers
;; =============================================================================

;; =============================================================================
;; API
;; =============================================================================

(defn application
  [current-steps]
  (let [active (common/active-step current-steps)]
    (common/application
     current-steps
     [:div.row
      [:div.col-xs-12
       [:a.btn.btn-lg.btn-success
        {:href (common/uri-for-step active)} (if (= active :logistics)
                                               "Start Now"
                                               "Resume")]]])))
