(ns starcity.views.terms.content)

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- make-term
  [title term]
  [title term])

;; =============================================================================
;; API
;; =============================================================================

(def preamble
  "Although this document (“Terms” or “Agreement”) forms a legal contract—and inevitably reads like a legal contract— the bulk of the clauses are simply designed to ensure you have a clear idea of how to use our Services, and what you can expect from Starcity Properties, Inc. (“Starcity”, “we”, “us” or “our”). Please read these Terms of Service (“Terms” or “Agreement”) and Starcity Properties, Inc.’s (“Starcity,” “we”, “us” or “our”) Privacy Policy fully and carefully before using www.starcityproperties.com (the “Site”) and the services, features, content or applications offered by Starcity Properties, Inc. (together with the Site, the “Services”).")

(def acceptance-terms-service
  (make-term "Acceptance of Terms of Service"
             {:type    :list
              :content ["By registering for and/or using the Services in any manner, including but not limited to visiting or browsing the Site, you agree to these Terms and all other operating rules, policies and procedures that we may posted on the Site from time to time.  Any such posting is incorporated by reference and each of which may be updated from time to time without notice to you."
                        "Certain of the Services, such as being granted a membership in a Starcity community, may be subject to additional terms and conditions specified by us from time to time; your use of such Services is subject to those additional terms and conditions, which are also incorporated into these Terms of Service."
                        "These Terms apply to anyone who uses our Services, including, without limitation, users who contribute content, information, and other materials or services, registered or otherwise."]}))

(def eligibility
  (make-term "Eligibility"
             {:content "You represent and warrant that you are at least 18 years of age. If you are under age 18, you may not, under any circumstances or for any reason, use the Services. We may, in our sole discretion, refuse to offer the Services to any person or entity and change its eligibility criteria at any time. You are solely responsible for ensuring that these Terms are in compliance with all laws, rules and regulations applicable to you and the right to access the Services is revoked where these Terms of Service or use of the Services is prohibited or to the extent offering, sale or provision of the Services conflicts with any applicable law, rule or regulation. Further, the Services are offered only for your use, and not for the use or benefit of any third party."}))

(def registration
  (make-term "Registration"
             {:type    :paragraphs
              :content ["For certain Services, you may be required to register for an account on the Site (“Account”). You must provide accurate and complete information and keep your Account information updated. You shall not: (i) select or use as a username a name of another person with the intent to impersonate that person; (ii) use as a username a name subject to any rights of a person other than you without appropriate authorization; or (iii) use, as a username, a name that is otherwise offensive, vulgar or obscene."
                        "Your Account information may include information that is personal to you, such as your name, email address and phone number (collectively, “Account Information”). You may only provide your own Account Information details. You may not provide Account Information of any third person. You must update your Account Information to reflect any change to your personal details. If at any time any portion of your Account Information is inaccurate or incomplete, or if you otherwise violate these Terms of Service, we may in our sole discretion and without advance notice choose to suspend or terminate your access to Services, your Account or both."
                        "You are solely responsible for the activity that occurs on your Account, and for keeping your Account password secure. You may never use another person’s user account or registration information for the Services without permission. You must notify us immediately of any change in your eligibility to use the Services, breach of security or unauthorized use of your Account. You should never publish, distribute or post login information for your Account. You have the ability to delete your Account by sending a request to team@starcityproperties.com."]}))

(def starcity-membership
  (make-term "Starcity Membership"
             {:type    :lead-list
              :content ["As part of the Services, you may apply to become a resident of the Starcity Properties, Inc. community (“Starcity Membership”). Those who are accepted into the community become “Members”. Members have access to certain residential Services, including access to concierge services. You can learn more about our Services and communities on the Site. An application for Starcity Membership may involve several steps, including: (i) providing certain personal details, such as your name, email address and phone number; (ii) answering several optional questions about yourself, such as your housing preferences and motivation for communal living; (iii) providing payment information to a third-party payment processor that we use (the “Payment Processor”) such as direct debit information for an account with a financial institution or a credit card for the purpose of future processing of any payments to us that you authorize, such as the application fee, deposit and Starcity Membership fees; and (iv) electing to share certain personally identifiable information with third party websites, services or applications for the purposes of running a background or income check.  Once candidates are pre-qualified by Starcity, they advance to a brief video conference or in person interview, with either an existing Member of the community to which the applicant has applied, or with a Starcity team member if no Member is available. We reserve the right to review applications for Starcity Membership to confirm that the applicant meets the criteria for membership, as determined in our sole discretion. We may reject or approve applications for Starcity Membership in our sole discretion. If you are accepted for a Starcity Membership, you will have the option to review and agree to a Starcity Membership Agreement that, along with this Agreement, will govern your use of the Services."
                        ["Income Verification" "To ensure you are financially capable of meeting your obligations under this agreement, we use Plaid, one of Partner services, to retrieve income information from your online banking provider.  By using our Services, and thereby consenting to this Agreement, you expressly grant our Partner the right, power and authority to act on your behalf to access and transmit your data as reasonably necessary for our Partner to provide the information we request."]
                        ["Background Check" "As a measure of safety for all our community members, an investigative consumer report will be made regarding the applicant’s character, general reputation, personal characteristics, and mode of living.  The investigation is conducted by Inflection.com, Inc. (“Consumer Reporting Agency”), one of our Partners. You have the right to view your file that a Consumer Reporting Agency holds. By providing proper identification and duplication cost, you may obtain a copy of this information in person at the Consumer Reporting Agency’s regular business hours and after providing reasonable notice for your request. Additionally, you can make the same request via mail or request a summary of the file over the phone. The Consumer Reporting Agency can assist you in understanding your file, including coded information. You are allowed to have one additional person accompany you so long as they provide proper identification. The Consumer Reporting Agency’s physical address is: <u>555 Twin Dolphin Drive, STE 200, Redwood City, CA 94065</u>"]]}))

(def ^:private content
  (make-term "Content"
             {:type    :list
              :content [["Definition" "For purposes of these Terms of Service, the term “Content” includes, without limitation, information, data, text, photographs, videos, audio clips, written posts and comments, software, scripts, graphics, and interactive features generated, provided, or otherwise made accessible on or through the Services. For the purposes of this Agreement, “Content” also includes all User Content (as defined below)."]
                        ["Content" "You acknowledge that all Content accessed by you using the Services is at your own risk and you will be solely responsible for any damage or loss to you or any other party resulting therefrom. We do not guarantee that any Content you access on or through the Services is or will continue to be accurate."]
                        ["Notices and Restrictions" "The Services may contain Content specifically provided by us, our Partners or our users and such Content is protected by copyrights, trademarks, service marks, patents, trade secrets or other proprietary rights and laws. You shall abide by and maintain all copyright notices, information, and restrictions contained in any Content accessed through the Services."]
                        ["Use License" "Subject to these Terms, we grant each user of the Services a worldwide, non-exclusive, non-sublicensable and non-transferable license to use (i.e., to download and display locally) Content solely for purposes of using the Services. Use, reproduction, modification, distribution or storage of any Content for other than purposes of using the Services is expressly prohibited without prior written permission from us. You shall not sell, license, rent, or otherwise use or exploit any Content for commercial use or in any way that violates any third party right."]
                        ["Availability of Content" "We do not guarantee that any Content will be made available on the Site or through the Services. We reserve the right to, but do not have any obligation to, (i) remove, edit or modify any Content in our sole discretion, at any time, without notice to you and for any reason (including, but not limited to, upon receipt of claims or allegations from third parties or authorities relating to such Content or if we are concerned that you may have violated these Terms of Service), or for no reason at all and (ii) to remove or block any Content from the Services."]]}))

(def terms
  [acceptance-terms-service
   eligibility
   registration
   starcity-membership
   content])
