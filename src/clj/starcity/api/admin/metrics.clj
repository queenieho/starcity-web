(ns starcity.api.admin.metrics
  (:require [clj-time.coerce :as c]
            [clojure.spec :as s]
            [compojure.core :refer [defroutes GET]]
            [datomic.api :as d]
            [plumbing.core :as plumbing]
            [starcity.datomic :refer [conn]]
            [starcity.models
             [account :as account]
             [application :as app]]
            [starcity.util.response :as response]
            [taoensso.timbre :as timbre]
            [toolbelt.predicates :as p]))

;; =============================================================================
;; Handlers
;; =============================================================================

(defn overview
  [conn pstart pend]
  (let [db (d/db conn)]
    {:accounts/created     (account/total-created db pstart pend)
     :applications/created (app/total-created db pstart pend)}))

(s/fdef overview
        :args (s/cat :conn p/conn?
                     :pstart inst?
                     :pend inst?)
        :ret (s/keys :req [:accounts/total
                           :applications/total]))

;; =============================================================================
;; Routes
;; =============================================================================

(defn- format-params [params]
  (-> (plumbing/update-in-when params [:pstart] c/to-date)
      (plumbing/update-in-when [:pend] c/to-date)))

(defroutes routes
  (GET "/" []
       (fn [{params :params}]
         (let [{:keys [pstart pend]} (format-params params)
               now                   (java.util.Date.)
               pstart                (or pstart (beginning-of-month now))
               pend                  (or pend (end-of-month now))]
           (response/transit-ok {:result (overview conn pstart pend)})))))
