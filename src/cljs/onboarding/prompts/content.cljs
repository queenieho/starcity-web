(ns onboarding.prompts.content
  (:require [ant-ui.core :as a]
            [re-frame.core :refer [subscribe]]))

(defmulti content :keypath)

(defmethod content :default
  [{:keys [keypath]}]
  [:p "No content defined for " [:b (str keypath)]])

(defmulti next-button :keypath)

(defmethod next-button :default [prompt]
  (let [dirty       (subscribe [:prompt/dirty?])
        can-advance (subscribe [:prompt/can-advance?])
        is-saving   (subscribe [:prompt/saving?])]
    [a/button {:type      :primary
               :size      :large
               :disabled  (not @can-advance)
               :loading   @is-saving
               :html-type :submit}
     (if @dirty
       [:span {:dangerouslySetInnerHTML {:__html "Save &amp; Continue"}}]
       "Continue")]))
