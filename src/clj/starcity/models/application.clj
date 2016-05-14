(ns starcity.models.application
  (:require [datomic.api :as d]
            [starcity.datomic.util :refer :all]
            [starcity.models.util :refer :all]
            [plumbing.core :refer [assoc-when defnk]]
            [schema.core :as s]
            [clojure.string :refer [trim capitalize]]))

;; =============================================================================
;; Helpers

(defn- scrub-name [s]
  (when-not (empty? s)
    (-> s capitalize trim)))

;; =============================================================================
;; Construct a New Rental Application

(defnk ^:private make-license
  [number state]
  (mapify :drivers-license {:number number :state state}))

(defnk ^:private make-phone
  [number type priority]
  (mapify :phone {:number number :type type :priority priority}))

(defn- make-application
  [{:keys [first-name middle-name last-name ssn license phones]}]
  (let [license (when license (make-license license))
        phones  (when phones (map make-phone phones))
        m       (assoc-when {:first-name (scrub-name first-name)
                             :last-name  (scrub-name last-name)
                             :ssn        (trim ssn)}
                            :middle-name (scrub-name middle-name)
                            :license license
                            :phones phones)]
    (mapify :rental-application m)))

;; =============================================================================
;; API

;; 1. We need a way to create a new rental application. What should be required?

(def ^:private CreateParams
  {:first-name                   s/Str
   :last-name                    s/Str
   :ssn                          s/Str
   (s/optional-key :middle-name) s/Str
   (s/optional-key :license)     {:number s/Str
                                  :state  s/Keyword}
   (s/optional-key :phones)      [{:number   s/Str
                                   :priority (s/enum :primary :secondary)
                                   :type     (s/enum :cell :home :work)}]})

(s/defn create!
  "Create a new rental application for the specified account."
  [{:keys [conn part] :as db} account :- Entity params :- CreateParams]
  (let [application (assoc (make-application params)
                           :account/_application (:db/id account))
        tid         (d/tempid part)
        tx          @(d/transact conn [(assoc application :db/id tid)])]
    (d/resolve-tempid (d/db conn) (:tempids tx) tid)))

(comment

  (def db* (:datomic user/system))


  )
