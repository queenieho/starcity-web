(ns starcity.controllers.onboarding
  (:require [blueprints.models.account :as account]
            [blueprints.models.approval :as approval]
            [blueprints.models.property :as property]
            [blueprints.models.security-deposit :as deposit]
            [datomic.api :as d]
            [facade.core :as facade]
            [facade.snippets :as snippets]
            [net.cgrand.enlive-html :as html]
            [starcity.config :as config :refer [config]]
            [starcity.controllers.common :as common]
            [starcity.datomic :refer [conn]]
            [starcity.util.request :as req]))

(html/defsnippet content "templates/onboarding.html" [:section] []
  [:section] (html/append (snippets/loading-fullscreen)))

(def ^:private move-in
  (comp approval/move-in approval/by-account))

(def ^:private full-deposit
  (comp deposit/required deposit/by-account))

(def ^:private llc
  (comp property/llc approval/property approval/by-account))

(defn show
  "Show the Onboarding app."
  [req]
  (let [account (req/requester (d/db conn) req)]
    (-> (facade/app req "onboarding"
                    :content (content)
                    :navbar (snippets/app-navbar)
                    :chatlio? true
                    :json [["stripe" {:key (config/stripe-public-key config)}]
                           ["account" {:move-in      (move-in account)
                                       :full-deposit (full-deposit account)
                                       :llc          (llc account)
                                       :name         (account/full-name account)
                                       :email        (account/email account)}]]
                    :css-bundles ["antd.css" "styles.css"]
                    :stylesheets [facade/font-awesome])
        (common/render-ok))))
