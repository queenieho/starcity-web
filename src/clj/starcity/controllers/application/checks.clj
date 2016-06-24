(ns starcity.controllers.application.checks
  (:require [starcity.views.application.checks :as view]
            [starcity.controllers.utils :refer :all]
            [starcity.models.property :as p]
            [starcity.models.application :as application]
            [starcity.datomic :refer [conn]]
            [starcity.datomic.util :refer :all]
            [datomic.api :as d]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [ring.util.response :as response]
            [clojure.spec :as s]))

;; =============================================================================
;; API
;; =============================================================================

(defn show-checks
  ""
  [{:keys [identity] :as req}]
  (ok
   ;; (view/checks {:first "Joshua" :middle "Adam" :last "Lehman"}
   ;;              {:lines      ["7255 Wild Currant Way" "Lower Floor"]
   ;;               :city        "Oakland"
   ;;               :state       "CA"
   ;;               :postal-code "94611"}
   ;;              "123-45-1610" "1989-03-15" "80k-90k")
   (view/checks {:first "Josh" :last "Lehman"} {} nil nil nil)
   ))

(defn save!
  ""
  [{:keys [identity params] :as req}]
  (clojure.pprint/pprint params)
  (response/redirect "/application/checks"))
