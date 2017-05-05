(ns onboarding.prompts.content
  (:require [ant-ui.core :as a]
            [re-frame.core :refer [subscribe]]))

(defmulti content :keypath)

(defmethod content :default
  [{:keys [keypath]}]
  [:p "No content defined for " [:b (str keypath)]])

(defmethod content :overview/start
  [_]
  [:div.content
   [:p "Before you can move in we need to collect your " [:b "security deposit"]
    "; luckily, our process is both simple and flexible."]
   [:p "Starcity offers two options for paying your security deposit. You can either:"]
   [:ul
    [:li "Pay " [:b "$500"] " towards your deposit now, and the rest "
     [:b "at the end of your first month"] ", or"]
    [:li "Pay the entire amount up front."]]
   [:p "You can pay your deposit online by linking your bank account " [:i "or"]
    " the traditional way by giving us a check."]
   [:p "Starcity also offers a variety of " [:b "premium services"]
    " that can help to simplify your move-in experience, trick out your room and more."]
   [:p "Complete each prompt using the blue " [:b "Continue"] " button in the bottom right "
    "corner of the screen, or jump around using the menu at left. After you have completed "
    "all of the prompts, you'll officially become a Starcity member."]
   [:p "Let's get started!"]])

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
