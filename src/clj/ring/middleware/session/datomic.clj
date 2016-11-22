(ns ring.middleware.session.datomic
  (:require [taoensso.nippy :as nippy]
            [ring.middleware.session.store :refer [SessionStore]]
            [datomic.api :as d]))

(defrecord DatomicStore [conn opts]
  SessionStore
  (read-session [_ key]
    ((:entity->session opts :session/value)
     (let [s       (d/pull (d/db conn) [:*] [:session/key key])
           s-value (:session/value s)]
       (assoc s :session/value
              (and s-value (nippy/thaw s-value))))))
  (delete-session [_ key]
    @(d/transact conn [[:db.fn/retractEntity [:session/key key]]])
    key)
  (write-session [_ key value]
    (let [key (or key
                  (when-let [session-key (:session/key opts)]
                    (session-key value))
                  (str (java.util.UUID/randomUUID)))]
      @(d/transact
        conn
        [(-> (if-let [session->entity (:session->entity opts)]
               (session->entity value)
               {:session/value value})
             (assoc :db/id (d/tempid (or (:partition opts) :db.part/user)))
             (assoc :session/key key)
             (update :session/value nippy/freeze))])
      key)))

(defn datomic-store [conn & {:as opts}]
  (DatomicStore. conn opts))
