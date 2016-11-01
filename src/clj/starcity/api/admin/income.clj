(ns starcity.api.admin.income
  (:require [clojure.spec :as s]
            [compojure.core :refer [defroutes GET]]
            [datomic.api :as d]
            [ring.util.response :as response]
            [starcity
             [datomic :refer [conn]]
             [util :refer [str->int]]]))

;; =============================================================================
;; API
;; =============================================================================

(defn fetch-file
  "Fetch an income file by `file-id` ."
  [file-id]
  (let [file (d/entity (d/db conn) file-id)]
    (response/file-response (:income-file/path file))))

(s/fdef fetch-file
        :args (s/cat :file-id integer?))

(defroutes routes
  (GET "/:file-id" [file-id]
       (fn [_] (fetch-file (str->int file-id)))))
