(ns starcity.events.account
  (:require [clojure.core.async :refer [go]]
            [dire.core :refer [with-pre-hook!]]
            [datomic.api :as d]
            [starcity.datomic :refer [conn]]
            [taoensso.timbre :as timbre]))

(defn deauthorize!
  [account]
  (go
    (when-let [session-id (d/q '[:find ?e .
                                 :in $ ?a
                                 :where
                                 [?e :session/account ?a]]
                               (d/db conn) (:db/id account))]
      @(d/transact conn [[:db.fn/retractEntity session-id]]))))

(with-pre-hook! #'deauthorize!
  (fn [account] (timbre/info ::deauthorize {:account (:db/id account)
                                           :email   (:account/email account)})))
