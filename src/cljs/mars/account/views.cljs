(ns mars.account.views
  (:require [re-frame.core :refer [subscribe dispatch]]
            [mars.account.rent.views :as rent]))

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
        :rent [rent/view]
        [default-view]))))
