(ns mars.account.views
  (:require [re-frame.core :refer [subscribe dispatch]]
            [mars.account.rent.views :as rent]
            [mars.account.settings.views :as settings]))

(defn default-view []
  [:div
   [:section.section.banner
    [:h1.title.is-1 [:strong "Account"]]]
   [:section.section.content
    [:p "Default stuff!"]]])

(defn account []
  (let [subsection (subscribe [:account/subsection])]
    (fn []
      (case @subsection
        :rent     [rent/view]
        :settings [settings/view]
        [default-view]))))
