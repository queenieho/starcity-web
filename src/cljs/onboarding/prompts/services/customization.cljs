(ns onboarding.prompts.services.customization
  (:require [ant-ui.core :as a]
            [onboarding.prompts.content :as content]
            [re-frame.core :refer [dispatch subscribe]]
            [toolbelt.core :as tb]))

(def ^:private labels
  {:furniture "Would you like to customize your room layout, furniture arrangement, or request a specific furniture item?"
   :design    "Your room comes fully furnished and decorated. Please let us know if you would like to personalize the room to reflect your personality and taste. From painting accent colors, and curating art from local artists to hanging/framing your own art, we've got you covered."
   })

(def ^:private placeholder
  "Please leave a detailed description about what you'd like to accomplish and we'll get you a quote within 24 hours.")

(defn- form
  [keypath {:keys [needed furniture design]}]
  [a/card
   [:div.control
    [:label.label "Would you like to customize your room?"]
    [a/radio-group
     {:on-change #(dispatch [:prompt/update keypath :needed (= (.. % -target -value) "yes")])
      :value     (cond (true? needed) "yes" (false? needed) "no" :otherwise nil)}
     [a/radio {:value "yes"} "Yes"]
     [a/radio {:value "no"} "No"]]]
   (when needed
     [:div
      [:div.control
       [:label.label (:furniture labels)]
       [a/input {:type          :textarea
                 :default-value furniture
                 :placeholder   placeholder
                 :rows          4
                 :on-change     #(dispatch [:prompt/update keypath :furniture (.. % -target -value)])}]]
      [:div.control
       [:label.label (:design labels)]
       [a/input {:type          :textarea
                 :default-value design
                 :placeholder   placeholder
                 :rows          4
                 :on-change     #(dispatch [:prompt/update keypath :furniture (.. % -target -value)])}]]])])

(def ^:private description
  "Starcity provides fully furnished suites. If you want to make your suite a
  reflection of your personality and design it to accomodate your needs, we're here to help.")

(defmethod content/content :services/customization
  [{:keys [keypath data] :as item}]
  [:div.content
   [:p {:dangerouslySetInnerHTML {:__html description}}]
   [form keypath data]])
