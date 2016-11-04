(ns starcity.api.admin.accounts
  (:require [compojure.core :refer [context defroutes GET]]
            [starcity.api.admin.accounts
             [entry :as entry]
             [list :as list]
             [security-deposit :as security-deposit]]
            [starcity.util :refer [str->int]]))

(defroutes routes
  (GET "/" [limit offset direction sort-key view]
       (fn [_] (list/fetch (str->int limit)
                          (str->int offset)
                          (keyword direction)
                          (keyword sort-key)
                          (keyword view))))

  (context "/:account-id" [account-id]
           (GET "/" [] (fn [_] (entry/fetch (str->int account-id))))

           (context "/security-deposit" [] security-deposit/routes)))
