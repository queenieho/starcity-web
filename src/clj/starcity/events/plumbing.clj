(ns starcity.events.plumbing
  (:require [taoensso.timbre :as timbre]
            [clojure.core.async :as a :refer [go <! >! go-loop chan]]
            [starcity.util :refer [entity?]]
            [mount.core :refer [defstate]]))

;; =============================================================================
;; Internal
;; =============================================================================

;; =============================================================================
;; Event Bus

(defstate channel
  :start (chan (a/sliding-buffer 4096))
  :stop (a/close! channel))

(defstate bus
  :start (a/mult channel))

;; =============================================================================
;; Helpers

(defn log-params [m]
  (reduce
   (fn [acc [k v]]
     (if (entity? v)
       (assoc acc k (:db/id v))
       (assoc acc k v)))
   {}
   m))

(defn make-queue
  [name in-c handler]
  (go-loop []
    (let [v (<! in-c)]
      (if (nil? v)
        (timbre/tracef "%s queue shutting down" name)
        (do
          (try
            (handler v)
            (catch Throwable t
              (timbre/errorf t "error encountered on %s queue" name)))
          (recur))))))

;; =============================================================================
;; API
;; =============================================================================

(defmacro defproducer [fn-name event-key params & body]
  `(defn ~fn-name ~params
     (let [event-params# (zipmap ~(mapv keyword params) ~params)]
       (timbre/info ~event-key (log-params event-params#))
       (go
         (try
           (let [res# (do ~@body)
                 evt# (if (and (map? res#) (contains? res# :result))
                        (assoc res# :event ~event-key)
                        (merge {:result res# :event ~event-key} event-params#))]
             (timbre/debugf "'%s' event produced: %s" ~(name event-key) evt#)
             (>! channel evt#)
             res#)
           (catch Throwable t#
             (timbre/error t# ~event-key (log-params event-params#))
             t#))))))

(defmacro defobserver
  ([name handler]
   `(defobserver ~name (chan) ~handler))
  ([name channel handler]
   `(defstate ~name
      :start (let [c# ~channel]
               (a/tap bus c#)
               (make-queue ~(keyword name) c# ~handler)
               c#)
      :stop (do
              (a/untap bus ~name)
              (a/close! ~name)))))
