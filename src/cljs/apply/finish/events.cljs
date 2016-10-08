(ns apply.finish.events
  (:require [apply.prompts.models :as prompts]
            [apply.routes :as routes]
            [re-frame.core :refer [reg-event-db
                                   reg-event-fx
                                   inject-cofx]]
            [day8.re-frame.http-fx]
            [starcity.log :as l]
            [ajax.core :as ajax]
            [apply.notifications :as n]))

;; =============================================================================
;; Editing

(reg-event-db
 :finish.pay/toggle-agree
 (fn [db _]
   (update-in db [:finish/pay :agreed-to-terms] not)))

(reg-event-fx
 :finish/begin-checkout
 (fn [{:keys [db]} _]
   (let [agreed-to-terms (get-in db [:finish/pay :agreed-to-terms])]
     ;; only allow the stripe checkout when terms have been agreed to
     (when agreed-to-terms
       {:stripe-checkout {:on-success [:finish/submit-payment]}}))))

(reg-event-fx
 :finish/submit-payment
 (fn [cofx [_ token]]
   {:http-xhrio {:method          :post
                 :uri             "/api/v1/apply/submit-payment"
                 :params          {:token (:id token)}
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-failure      [:finish.submit-payment/failure]
                 :on-success      [:finish.submit-payment/success]}}))

(def ^:private payment-error-msg
  "Something went wrong while processing your payment. Please try again.")

(reg-event-fx
 :finish.submit-payment/failure
 (fn [cofx [_ err]]
   (l/error "Error encountered while submitting payment" err)
   {:dispatch [:app/notify (n/error payment-error-msg)]}))

;; On success, just transition the URL to the "complete" view
;; There's no need for actually doing validation that the application has been
;; successfully completed, since the URL is not exposed anywhere.
(reg-event-fx
 :finish.submit-payment/success
 (fn [_ [_ result]]
   {:route (routes/complete)}))
