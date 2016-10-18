(ns starcity.controllers.apply
  (:require [starcity.views.apply :as view]
            [starcity.controllers.utils :refer [ok]]))

(defn show-apply
  [{:keys [identity params] :as req}]
  (ok (view/apply (:account/email identity))))
