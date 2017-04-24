(ns onboarding.prompts.services.customize
  (:require [onboarding.components.catalogue :as catalogue]
            [onboarding.prompts.content :as content]))

(def ^:private description
  "Starcity provides fully furnished suites. If you want to make your suite a
  reflection of your personality and design it to accomodate your needs, we're here to help.")

(defmethod content/content :services/customize
  [{:keys [keypath data] :as item}]
  (let [{:keys [orders catalogue]} data]
    [:div.content
     [:p {:dangerouslySetInnerHTML {:__html description}}]
     [catalogue/grid keypath catalogue orders :grid-size 1]]))
