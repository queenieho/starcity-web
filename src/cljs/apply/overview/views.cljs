(ns apply.overview.views
  (:require [apply.prompts.views :as p]
            [apply.routes :refer [prompt-uri]]))

(defn welcome []
  (p/prompt
   (p/header "Account Verified!")
   (p/content
    [:div.content
     [:p "Congratulations! You've taken the first step towards joining a Starcity community. We're looking forward to learning more about you."]

     [:p "Here's a quick overview of our application process to get you comfortable with what's ahead."]

     [:ul
      ;; Logistics
      [:li "Let us know which communities you'd like to join, youre preferred
      move-in date, and how long you'll be staying with us "
       [:span {:dangerouslySetInnerHTML {:__html "&mdash; "}}]
       "we'll handle the " [:strong "logistics"] "."]
      ;; Personal information
      [:li "Provide us with a few pieces of "
       [:strong "personal information"]
       " so that we can perform a background check on you "
       [:span {:dangerouslySetInnerHTML {:__html "&mdash; "}}]
       " this is an important part of what keeps Starcity communities safe."]
      ;; Community fitness
      [:li "Each of our communities is uniquely cultivated by our members. "
       [:strong "Community fitness "]
       "is determined by community members using the responses you provide."]
      ;; Final Steps
      [:li "TODO: Talk about final steps wrt payment, tos, etc."]]])
   (p/footer :next-link :overview/advisor)))

(defn advisor []
  (p/prompt
   (p/header "Hi, I'm your Community Advisor!")
   (p/content
    [:div.content
     ;; NOTE: "My name is Mo. Nice to meet you!"
     [:p "Here's the idea: Have this be a welcome from the head of community
    advisors (or whatever), Mo, and mention that you'll see other community
    advisors specific to the community after making your selection. The
    introduction of the concept will also come from Mo."]
     [:p "On a related note, selection of communities could result in the
     community advisors being shown (or we could just show them there by
     default.)"]])
   (p/footer :next-link :logistics/communities :next-label "Begin")))
