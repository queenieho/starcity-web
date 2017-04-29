(ns starcity.controllers.onboarding
  (:require [net.cgrand.enlive-html :as html]
            [optimus.link :as link]
            [starcity.controllers.common :as common]
            [starcity.config.stripe :refer [public-key]]
            [starcity.views.base :as base]
            [starcity.models.approval :as approval]
            [starcity.auth :as auth]
            [starcity.models.security-deposit :as deposit]
            [starcity.models.property :as property]))

(html/defsnippet content "templates/onboarding.html" [:section] []
  [:section] (html/append (base/loading-fs)))

(def ^:private move-in
  (comp approval/move-in approval/by-account))

(def ^:private full-deposit
  (comp deposit/required deposit/by-account))

(def ^:private llc
  (comp property/llc approval/property approval/by-account))

(defn show
  "Show the Onboarding app."
  [req]
  (let [account (auth/requester req)]
    (-> (base/app-base req "onboarding"
                       :content (content)
                       :navbar (base/app-navbar)
                       :json [["stripe" {:key public-key}]
                              ["account" {:move-in      (move-in account)
                                          :full-deposit (full-deposit account)
                                          :llc          (llc account)}]]
                       :stylesheets (concat
                                     (link/bundle-paths req ["antd.css"])
                                     [base/font-awesome]))
        (common/render-ok))))
