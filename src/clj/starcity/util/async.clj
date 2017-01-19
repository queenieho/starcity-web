(ns starcity.util.async
  (:require [clojure.core.async :as a :refer [<!! <! go]]))

(defn chan? [x]
  (satisfies? clojure.core.async.impl.protocols/Channel x))

(defmacro <!!? [c]
  `(let [v# (<!! ~c)]
     (if (instance? Throwable v#)
       (throw v#)
       v#)))

(defmacro <!? [c]
  `(let [v# (<! ~c)]
     (if (instance? Throwable v#)
       (throw v#)
       v#)))

(defmacro go-try
  "Wrap `body` in a `try-catch` block, causing just the Throwable be produced by
  the `go` block iff there is one."
  [& body]
  `(go (try ~@body
            (catch Throwable ex#
              ex#))))
