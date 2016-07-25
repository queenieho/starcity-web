(ns starcity.views.terms
  (:require [starcity.views.base :refer [base]]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defmulti render-term-content :type)

(defmethod render-term-content :list
  [{content :content}]
  [:ol (for [c content] [:li c])])

(defmethod render-term-content :paragraphs
  [{content :content}]
  (list
   [:span (first content)]
   (for [p (rest content)]
     [:p p])))

(defmethod render-term-content nil
  [{content :content}]
  [:span (str " " content)])

(defn- render-term [[title content]]
  [:li [:strong (str title ":")] (render-term-content content)])

;; =============================================================================
;; Content
;; =============================================================================

(def ^:private preamble
  "Although this document (“Terms” or “Agreement”) forms a legal contract—and inevitably reads like a legal contract— the bulk of the clauses are simply designed to ensure you have a clear idea of how to use our Services, and what you can expect from Starcity Properties, Inc. (“Starcity”, “we”, “us” or “our”). Please read these Terms of Service (“Terms” or “Agreement”) and Starcity Properties, Inc.’s (“Starcity,” “we”, “us” or “our”) Privacy Policy fully and carefully before using www.starcityproperties.com (the “Site”) and the services, features, content or applications offered by Starcity Properties, Inc. (together with the Site, the “Services”).")

(def ^:private terms
  [["Acceptance of Terms of Service"
    {:type    :list
     :content ["By registering for and/or using the Services in any manner, including but not limited to visiting or browsing the Site, you agree to these Terms and all other operating rules, policies and procedures that we may posted on the Site from time to time.  Any such posting is incorporated by reference and each of which may be updated from time to time without notice to you."
               "Certain of the Services, such as being granted a membership in a Starcity community, may be subject to additional terms and conditions specified by us from time to time; your use of such Services is subject to those additional terms and conditions, which are also incorporated into these Terms of Service."
               "These Terms apply to anyone who uses our Services, including, without limitation, users who contribute content, information, and other materials or services, registered or otherwise."]}]
   ["Eligibility"
    {:content "You represent and warrant that you are at least 18 years of age. If you are under age 18, you may not, under any circumstances or for any reason, use the Services. We may, in our sole discretion, refuse to offer the Services to any person or entity and change its eligibility criteria at any time. You are solely responsible for ensuring that these Terms are in compliance with all laws, rules and regulations applicable to you and the right to access the Services is revoked where these Terms of Service or use of the Services is prohibited or to the extent offering, sale or provision of the Services conflicts with any applicable law, rule or regulation. Further, the Services are offered only for your use, and not for the use or benefit of any third party."}]
   ["Registration"
    {:type :paragraphs
     :content ["For certain Services, you may be required to register for an account on the Site (“Account”). You must provide accurate and complete information and keep your Account information updated. You shall not: (i) select or use as a username a name of another person with the intent to impersonate that person; (ii) use as a username a name subject to any rights of a person other than you without appropriate authorization; or (iii) use, as a username, a name that is otherwise offensive, vulgar or obscene."
               "Your Account information may include information that is personal to you, such as your name, email address and phone number (collectively, “Account Information”). You may only provide your own Account Information details. You may not provide Account Information of any third person. You must update your Account Information to reflect any change to your personal details. If at any time any portion of your Account Information is inaccurate or incomplete, or if you otherwise violate these Terms of Service, we may in our sole discretion and without advance notice choose to suspend or terminate your access to Services, your Account or both."
               "You are solely responsible for the activity that occurs on your Account, and for keeping your Account password secure. You may never use another person’s user account or registration information for the Services without permission. You must notify us immediately of any change in your eligibility to use the Services, breach of security or unauthorized use of your Account. You should never publish, distribute or post login information for your Account. You have the ability to delete your Account by sending a request to team@starcityproperties.com."]}]])


(def ^:private content
  [:main
   [:div.container
    [:div.center
     [:h3 "Starcity Properties, Inc"]
     [:h4 "Terms of Service"]]
    [:div.divider]
    [:p preamble]
    [:ol (map render-term terms)]]])

;; =============================================================================
;; API
;; =============================================================================

(defn terms
  []
  (base :content content))
