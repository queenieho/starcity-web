(ns starcity.observers
  (:require [clojure.core.async :as a]
            [datomic.api :as d]
            [mount.core :refer [defstate]]
            [plumbing.core :refer [update-in-when]]
            [starcity.datomic :refer [conn listener]]
            [starcity.models.msg :as msg]
            [starcity.observers
             [cmds :as cmds]
             [mailer :as mailer]
             [slack :as slack]]
            [taoensso
             [nippy :as nippy]
             [timbre :as timbre]]))

;; =============================================================================
;; Internal
;; =============================================================================

;; =============================================================================
;; Helpers

(defn- extract-by-attr
  "Given an attribute `attr`, produce entity from transaction report `txr` that
  contain `attr`."
  [attr txr]
  (let [attr-id (d/entid (:db-after txr) attr)]
    (some (fn [[e a _ _ _]] (when (= a attr-id) e)) (:tx-data txr))))

;; =============================================================================
;; Observer Lifecycle

(defn- make-observer!
  "Construct a new observer on `listener`. All transaction reports received are
  passed through `extraction-fn`, which is expected to produce entity ids.
  Constructed entities are then passed to `dispatch-fn` for processing."
  ([listener extraction-fn dispatch-fn]
   (make-observer! listener extraction-fn dispatch-fn 4096))
  ([listener extraction-fn dispatch-fn buf-size]
   (let [c (a/chan (a/sliding-buffer buf-size))]
     (a/go-loop []
       (when-let [txr (a/<! c)]
         (try
           (when-let [entity-id (extraction-fn txr)]
             (let [entity (d/entity (:db-after txr) entity-id)]
               (dispatch-fn entity)))
           (catch Throwable t
             (timbre/error t "observer error!")))
         ;; NOTE: The recur MUST happen inside of the when-let. Otherwise the
         ;; go-loop indefinitely recurs after the channel closes, hogging CPU.
         (recur)))
     (a/tap listener c)
     c)))

(defn- close-observer!
  "Clean up observer resources."
  [listener observer]
  (a/untap listener observer)
  (a/close! observer))

;; =============================================================================
;; Observers
;; =============================================================================

;; =============================================================================
;; Middleware

(defn- wrap-mapify
  "Converts an event (`cmd` or `msg`) into a map with `ks`."
  [f & ks]
  (fn [evt]
    (f (select-keys evt ks))))

(defn- wrap-log
  "Logs event `evt` using the event key found under `log-key`, and logs that
  data found by extracting keys `ks`."
  [f log-key & ks]
  (fn [evt]
    (do
      (timbre/trace (get evt log-key) (select-keys evt ks))
      (f evt))))

;; TODO: Remove after cmds and msgs are converted to use of `data` and `ctx`
;; over `params` and `meta`
(defn- wrap-thaw
  "Thaws the values in `evt` under `ks`."
  [f & ks]
  (fn [evt]
    (let [thaw? (set ks)]
      (f (reduce
          (fn [acc [k v]]
            (if (thaw? k)
              (update-in-when acc [k] nippy/thaw)
              (assoc acc k v)))
          evt
          evt)))))

(defn- wrap-deserialize
  "Deserializes the values in `evt` under `ks`."
  [f & ks]
  (fn [evt]
    (let [deserialize? (set ks)]
      (f (reduce
          (fn [acc [k v]]
            (if (deserialize? k)
              (update-in-when acc [k] read-string)
              (assoc acc k v)))
          evt
          evt)))))

;; =============================================================================
;; cmd

(defn- pending-cmds [txr]
  (let [cmd-status-attr-id (d/entid (:db-after txr) :cmd/status)
        pending-status-id    (d/entid (:db-after txr) :cmd.status/pending)]
    (some (fn [[e a v _ add :as datom]]
            (when (and (= a cmd-status-attr-id)
                       (= v pending-status-id)
                       add)
              e))
          (:tx-data txr))))

(defstate cmd-processor
  "Processes incoming cmds with status `:cmd.status/pending`."
  :start (make-observer! listener
                         pending-cmds
                         (-> (partial cmds/handle conn)
                             (wrap-log :cmd/key :cmd/uuid :cmd/data :cmd/ctx
                                       :cmd/params :cmd/meta)
                             (wrap-thaw :cmd/params :cmd/meta)
                             (wrap-deserialize :cmd/data :cmd/ctx)
                             (wrap-mapify :db/id :cmd/key :cmd/uuid :cmd/meta
                                          :cmd/id :cmd/params :cmd/status
                                          :cmd/data :cmd/ctx))
                         8192)
  :stop (close-observer! listener cmd-processor))

;; =============================================================================
;; msg

(defn- wrap-thaw-msg [f]
  (fn [msg]
    (-> (select-keys msg [:db/id :msg/key :msg/uuid :msg/params])
        (update-in-when [:msg/params] nippy/thaw)
        f)))

(defn- log-msgs [msg]
  (timbre/trace (:msg/key msg) (select-keys msg [:msg/uuid :msg/params])))

(def msg-extractor (partial extract-by-attr :msg/key))

(defn wrap-msg [handler]
  (-> handler
      (wrap-log :msg/key :msg/uuid :msg/data
                :msg/params)
      (wrap-thaw :msg/params)
      (wrap-deserialize :msg/data)
      (wrap-mapify :db/id :msg/key :msg/uuid :msg/data :msg/params)))

(defstate slack
  "Sends Slack messages for matching `msg`."
  :start (make-observer! listener
                         msg-extractor
                         (wrap-msg (partial slack/handle conn)))
  :stop (close-observer! listener slack))

(defstate mailer
  "Sends emails for matching `msg`."
  :start (make-observer! listener
                         msg-extractor
                         (wrap-msg (partial mailer/handle conn)))
  :stop (close-observer! listener mailer))

(comment
  @(d/transact conn [(starcity.models.cmd/create :test :params {:hello "world"})])

  (d/transact conn [{:db/id        [:cmd/uuid #uuid "58a37e89-df76-4bf6-8aa4-ab7a497158d2"]
                     :cmd/status :cmd.status/pending}])

  (d/entity (d/db conn) [:cmd/uuid #uuid "58a37e89-df76-4bf6-8aa4-ab7a497158d2"])

  )
