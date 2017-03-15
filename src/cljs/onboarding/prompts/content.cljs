(ns onboarding.prompts.content)

(defmulti content :keypath)

(defmethod content :default
  [{:keys [keypath]}]
  [:p "No content defined for " [:b (str keypath)]])
