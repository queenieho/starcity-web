(ns starcity.api.mars.rent.payments
  (:require [compojure.core :refer [defroutes GET POST]]
            [starcity
             [auth :as auth]
             [datomic :refer [conn]]]
            [starcity.api.common :refer :all]
            [starcity.models.rent :as rent]
            [starcity.events.rent :refer [make-ach-payment!]]
            [starcity.util :refer :all]
            [datomic.api :as d]
            [taoensso.timbre :as timbre]))

;;; Next Payment

(defn next-payment-handler
  "Retrieve the details of the 'next' payment for requesting account."
  [{:keys [params] :as req}]
  (let [account (auth/requester req)]
    (ok (rent/next-payment conn account))))

;;; Payments List

(defn- clientize-payment-item
  [{:keys [:db/id
           :rent-payment/amount
           :rent-payment/method
           :rent-payment/status
           :rent-payment/period-start
           :rent-payment/period-end
           :rent-payment/paid-on
           :rent-payment/late
           :rent-payment/due-date
           :rent-payment/check
           :rent-payment/method-desc]}]
  (merge {:id     id
          :status (name status)
          :pstart period-start
          :pend   period-end
          :late   late
          :due    due-date
          :paid   paid-on
          :amount amount
          :desc   method-desc}
         (when method {:method (name method)})
         (when check {:check {:number (:check/number check)}})))

(defn payments-handler
  "Retrieve the list of rent payments for the requesting account."
  [req]
  (let [account (auth/requester req)]
    (ok {:payments (->> (rent/payments conn account)
                        (map clientize-payment-item))})))

;;; Make Payment

(defn- payment-entity [payment-id]
  (let [id (if (string? payment-id) (str->int payment-id) payment-id)]
    (d/entity (d/db conn) id)))

(defn make-payment-handler
  "TODO:"
  [{:keys [params] :as req}]
  (let [account (auth/requester req)
        payment (payment-entity (:payment-id params))]
    (try
      (let [_ (<!!? (make-ach-payment! account payment))]
        (ok {:message "success"}))
      (catch Throwable ex
        (server-error)))))

;;; Routes

(defroutes routes
  (GET "/" [] payments-handler)

  (GET "/next" [] next-payment-handler)

  (POST "/:payment-id/pay" [] make-payment-handler))
