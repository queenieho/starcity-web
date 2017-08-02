(ns starcity.api.admin.deposits
  (:require [blueprints.models.security-deposit :as deposit]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [compojure.core :refer [defroutes POST]]
            [datomic.api :as d]
            [reactor.events :as events]
            [starcity.datomic :refer [conn]]
            [starcity.util.response :as response]
            [starcity.util.validation :as uv]
            [toolbelt.core :as tb]))

(defn refund-validators [deposit]
  {:amount            [v/required v/number
                       [v/in-range [1 (deposit/amount deposit)]
                        :message (format "Refund amount must be in the range of $1 to $%.2f."
                                         (deposit/amount deposit))]]
   :deduction-reasons [[v/required
                        :pre (comp (partial not= (deposit/amount deposit)) float :amount)
                        :message "Reasons must be provided when a partial refund is issued."]
                       [v/string :message "Please supply reasons for the partial refund."]]})


(defn refund-deposit-handler
  [{:keys [params] :as req}]
  (let [deposit (d/entity (d/db conn) (tb/str->int (:deposit-id params)))
        vresult (b/validate params (refund-validators deposit))]
    (cond
      (some? (deposit/refund-status deposit))
      (response/transit-unprocessable
       {:error "This deposit cannot be refunded; it's likely that it already has been."})

      (not (uv/valid? vresult))
      (response/transit-malformed {:error (first (uv/errors vresult))})

      :otherwise
      (do
        @(d/transact conn [{:db/id                 (:db/id deposit)
                            :deposit/refund-status :deposit.refund-status/initiated}
                           (events/initiate-refund deposit (:amount params) (:deduction-reasons params))])
        (response/transit-ok {:message "OK"})))))


(defroutes routes
  (POST "/:deposit-id/refund" [] refund-deposit-handler))
