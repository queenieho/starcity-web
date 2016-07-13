(ns starcity.views.application.submit
  (:require [starcity.views.application.common :as common]
            [starcity.config :refer [config]]))

;; =============================================================================
;; Helpers
;; =============================================================================


;; =============================================================================
;; API
;; =============================================================================

(defn submit
  [current-steps email errors]
  (let [sections []]
    (common/application
     current-steps
     [:div.question-container
      (common/error-alerts errors)

      [:form {:method "POST" :action ""}
       [:ul.question-list
        (for [[title content] sections]
          (common/section title content))]
       [:input#stripe-token {:type "hidden" :name "stripe-token"}]
       (common/submit-button "Pay &amp; Submit" "checkout-btn")]]
     :title "Submit"
     :json [["stripe" {:key   (get-in config [:stripe :public-key])
                       :email email}]]
     :js ["https://checkout.stripe.com/checkout.js"
          "submit.js"])))
