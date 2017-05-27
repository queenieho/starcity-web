(ns starcity.controllers.onboarding
  (:require [facade
             [core :as facade]
             [snippets :as snippets]]
            [net.cgrand.enlive-html :as html]
            [starcity.auth :as auth]
            [starcity.config.stripe :refer [public-key]]
            [starcity.controllers.common :as common]
            [starcity.models
             [account :as account]
             [approval :as approval]
             [property :as property]
             [security-deposit :as deposit]]))

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
  (let [account (auth/requester req)]
    (-> (facade/app req "onboarding"
                    :content (content)
                    :navbar (snippets/app-navbar)
                    :chatlio? true
                    :json [["stripe" {:key public-key}]
                           ["account" {:move-in      (move-in account)
                                       :full-deposit (full-deposit account)
                                       :llc          (llc account)
                                       :name         (account/full-name account)
                                       :email        (account/email account)}]]
                    :css-bundles ["antd.css" "styles.css"]
                    :stylesheets [facade/font-awesome])
        (common/render-ok))))
