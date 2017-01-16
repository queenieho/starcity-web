(ns starcity.models.income-file
  (:require [clojure.java.io :as io]
            [datomic.api :as d]
            [me.raynes.fs :as fs]
            [starcity
             [config :refer [data-dir]]
             [datomic :refer [conn tempid]]]
            [starcity.models
             [account :as account]
             [util :refer [qes]]]
            [taoensso.timbre :as t]))

(defn- write-income-file
  "Write a an income file to the filesystem and add an entity that points to the
  account and file path."
  [account {:keys [filename content-type tempfile size]}]
  (try
    (let [output-dir  (format "%s/income-uploads/%s" data-dir (:db/id account))
          output-path (str output-dir "/" filename)]
      (do
        (when-not (fs/exists? output-dir)
          (fs/mkdirs output-dir))
        (io/copy tempfile (java.io.File. output-path))
        (let [tid    (tempid)
              tx     @(d/transact conn [{:income-file/account      (:db/id account)
                                         :income-file/content-type content-type
                                         :income-file/path         output-path
                                         :income-file/size         (long size)
                                         :db/id                    tid}])
              ent-id (d/resolve-tempid (d/db conn) (:tempids tx) tid)]
          (t/info ::write {:user         (account/email account)
                           :filename     filename
                           :content-type content-type
                           :size         size
                           :entity-id    ent-id})
          output-path)))
    ;; catch to log, then rethrow
    (catch Exception e
      (t/error e ::write {:user         (account/email account)
                          :filename     filename
                          :content-type content-type
                          :size         size})
      (throw e))))

(defn create
  "Save the income files for a given account."
  [account files]
  (doall (map (partial write-income-file account) files)))

(defn by-account
  "Fetch the income files for this account."
  [account]
  (qes '[:find ?e
         :in $ ?a
         :where
         [?e :income-file/account ?a]]
       (d/db conn) (:db/id account)))
