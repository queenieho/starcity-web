(ns starcity.api.admin.accounts.security-deposit
  (:require [compojure.core :refer [defroutes GET POST]]
            [starcity.util :refer [str->int]]
            [starcity.api.common :as api]
            [clojure.spec :as s]
            [starcity.models.security-deposit :as security-deposit]
            [starcity.models.stripe :as stripe]))

(def sample-charges
  [{:status  "pending"
    :amount  50000
    :id      "py_19BTBqIvRccmW9nOSPAkGWc9"
    :created 1478110222}])

(defn- fetch-charge [charge]
  (let [charge (stripe/fetch-charge charge)]
    (select-keys charge [:status :amount :id :created])))

(defn- fetch
  [account-id]
  (if-let [sd (security-deposit/lookup account-id)]
    (api/ok {:amount-received (get sd :security-deposit/amount-received 0)
             :amount-required (:security-deposit/amount-required sd)
             :payment-method  (when-let [method (:security-deposit/payment-method sd)]
                                (name method))
             :payment-type    (when-let [type (:security-deposit/payment-type sd)]
                                (name type))
             :charges         (map fetch-charge (:security-deposit/charges sd))})
    (api/ok {})))

(s/fdef fetch
        :args (s/cat :account-id integer?))

(defroutes routes
  (GET "/" [account-id] (fn [_] (fetch (str->int account-id)))))
