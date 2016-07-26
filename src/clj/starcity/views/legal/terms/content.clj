 (ns starcity.views.legal.terms.content
   (:require [starcity.views.legal.render :refer :all]
             [starcity.config :refer [config]]))

;; =============================================================================
;; Content
;; =============================================================================

(def preamble
  "Although this document (“Terms” or “Agreement”) forms a legal contract—and inevitably reads like a legal contract— the bulk of the clauses are simply designed to ensure you have a clear idea of how to use our Services, and what you can expect from Starcity Properties, Inc. (“Starcity”, “we”, “us” or “our”). Please read these Terms of Service (“Terms” or “Agreement”) and Starcity Properties, Inc.’s (“Starcity,” “we”, “us” or “our”) <PRIVACY POLICY> fully and carefully before using www.starcityproperties.com (the “Site”) and the services, features, content or applications offered by Starcity Properties, Inc. (together with the Site, the “Services”).")

(def acceptance-terms-service
  (with-title "Acceptance of Terms of Service"
    (list-content
     :alphabetical
     "By registering for and/or using the Services in any manner, including but not limited to visiting or browsing the Site, you agree to these Terms and all other operating rules, policies and procedures that we may posted on the Site from time to time.  Any such posting is incorporated by reference and each of which may be updated from time to time without notice to you."
     "Certain of the Services, such as being granted a membership in a Starcity community, may be subject to additional terms and conditions specified by us from time to time; your use of such Services is subject to those additional terms and conditions, which are also incorporated into these Terms of Service."
     "These Terms apply to anyone who uses our Services, including, without limitation, users who contribute content, information, and other materials or services, registered or otherwise.")))

(def eligibility
  (with-title "Eligibility"
    (content "You represent and warrant that you are at least 18 years of age. If you are under age 18, you may not, under any circumstances or for any reason, use the Services. We may, in our sole discretion, refuse to offer the Services to any person or entity and change its eligibility criteria at any time. You are solely responsible for ensuring that these Terms are in compliance with all laws, rules and regulations applicable to you and the right to access the Services is revoked where these Terms of Service or use of the Services is prohibited or to the extent offering, sale or provision of the Services conflicts with any applicable law, rule or regulation. Further, the Services are offered only for your use, and not for the use or benefit of any third party.")))

(def registration
  (with-title "Registration"
    (paragraphs-list
     "For certain Services, you may be required to register for an account on the Site (“Account”). You must provide accurate and complete information and keep your Account information updated. You shall not: (i) select or use as a username a name of another person with the intent to impersonate that person; (ii) use as a username a name subject to any rights of a person other than you without appropriate authorization; or (iii) use, as a username, a name that is otherwise offensive, vulgar or obscene."
     "Your Account information may include information that is personal to you, such as your name, email address and phone number (collectively, “Account Information”). You may only provide your own Account Information details. You may not provide Account Information of any third person. You must update your Account Information to reflect any change to your personal details. If at any time any portion of your Account Information is inaccurate or incomplete, or if you otherwise violate these Terms of Service, we may in our sole discretion and without advance notice choose to suspend or terminate your access to Services, your Account or both."
     "You are solely responsible for the activity that occurs on your Account, and for keeping your Account password secure. You may never use another person’s user account or registration information for the Services without permission. You must notify us immediately of any change in your eligibility to use the Services, breach of security or unauthorized use of your Account. You should never publish, distribute or post login information for your Account. You have the ability to delete your Account by sending a request to team@starcityproperties.com.")))

(def starcity-membership
  (with-title "Starcity Membership"
    (lead-list-content
     "As part of the Services, you may apply to become a resident of the Starcity Properties, Inc. community (“Starcity Membership”). Those who are accepted into the community become “Members”. Members have access to certain residential Services, including access to concierge services. You can learn more about our Services and communities on the Site. An application for Starcity Membership may involve several steps, including: (i) providing certain personal details, such as your name, email address and phone number; (ii) answering several optional questions about yourself, such as your housing preferences and motivation for communal living; (iii) providing payment information to a third-party payment processor that we use (the “Payment Processor”) such as direct debit information for an account with a financial institution or a credit card for the purpose of future processing of any payments to us that you authorize, such as the application fee, deposit and Starcity Membership fees; and (iv) electing to share certain personally identifiable information with third party websites, services or applications for the purposes of running a background or income check.  Once candidates are pre-qualified by Starcity, they advance to a brief video conference or in person interview, with either an existing Member of the community to which the applicant has applied, or with a Starcity team member if no Member is available. We reserve the right to review applications for Starcity Membership to confirm that the applicant meets the criteria for membership, as determined in our sole discretion. We may reject or approve applications for Starcity Membership in our sole discretion. If you are accepted for a Starcity Membership, you will have the option to review and agree to a Starcity Membership Agreement that, along with this Agreement, will govern your use of the Services."
     (with-title "Income Verification"
       "To ensure you are financially capable of meeting your obligations under this agreement, we use Plaid, one of Partner services, to retrieve income information from your online banking provider.  By using our Services, and thereby consenting to this Agreement, you expressly grant our Partner the right, power and authority to act on your behalf to access and transmit your data as reasonably necessary for our Partner to provide the information we request.")
     (with-title "Background Check"
       "As a measure of safety for all our community members, an investigative consumer report will be made regarding the applicant’s character, general reputation, personal characteristics, and mode of living.  The investigation is conducted by Inflection.com, Inc. (“Consumer Reporting Agency”), one of our Partners. You have the right to view your file that a Consumer Reporting Agency holds. By providing proper identification and duplication cost, you may obtain a copy of this information in person at the Consumer Reporting Agency’s regular business hours and after providing reasonable notice for your request. Additionally, you can make the same request via mail or request a summary of the file over the phone. The Consumer Reporting Agency can assist you in understanding your file, including coded information. You are allowed to have one additional person accompany you so long as they provide proper identification. The Consumer Reporting Agency’s physical address is: <u>555 Twin Dolphin Drive, STE 200, Redwood City, CA 94065</u>"))))

(def ^:private content-term
  (with-title "Content"
    (list-content
     :alphabetical
     (with-title "Definition"
       "For purposes of these Terms of Service, the term “Content” includes, without limitation, information, data, text, photographs, videos, audio clips, written posts and comments, software, scripts, graphics, and interactive features generated, provided, or otherwise made accessible on or through the Services. For the purposes of this Agreement, “Content” also includes all User Content (as defined below).")
     (with-title "Content"
       "You acknowledge that all Content accessed by you using the Services is at your own risk and you will be solely responsible for any damage or loss to you or any other party resulting therefrom. We do not guarantee that any Content you access on or through the Services is or will continue to be accurate.")
     (with-title "Notices and Restrictions"
       "The Services may contain Content specifically provided by us, our Partners or our users and such Content is protected by copyrights, trademarks, service marks, patents, trade secrets or other proprietary rights and laws. You shall abide by and maintain all copyright notices, information, and restrictions contained in any Content accessed through the Services.")
     (with-title "Use License"
       "Subject to these Terms, we grant each user of the Services a worldwide, non-exclusive, non-sublicensable and non-transferable license to use (i.e., to download and display locally) Content solely for purposes of using the Services. Use, reproduction, modification, distribution or storage of any Content for other than purposes of using the Services is expressly prohibited without prior written permission from us. You shall not sell, license, rent, or otherwise use or exploit any Content for commercial use or in any way that violates any third party right.")
     (with-title "Availability of Content"
       "We do not guarantee that any Content will be made available on the Site or through the Services. We reserve the right to, but do not have any obligation to, (i) remove, edit or modify any Content in our sole discretion, at any time, without notice to you and for any reason (including, but not limited to, upon receipt of claims or allegations from third parties or authorities relating to such Content or if we are concerned that you may have violated these Terms of Service), or for no reason at all and (ii) to remove or block any Content from the Services."))))

(def ^:private rules-of-conduct
  (with-title "Rules of Conduct"
    (list-content
     :alphabetical
     "As a condition of use, you promise not to use the Services for any purpose that is prohibited by these Terms of Service. You are responsible for all of your activity in connection with the Services."
     "You shall not (and shall not permit any third party to) either (a) take any action or (b) upload, download, post, submit or otherwise distribute or facilitate distribution of any Content on or through the Service, that:"
     (list-content
      :roman
      "infringes any patent, trademark, trade secret, copyright, right of publicity or other right of any other person or entity or violates any law or contractual duty;"
      "you know is false, misleading, untruthful or inaccurate;"
      "is unlawful, threatening, abusive, harassing, defamatory, libelous, deceptive, fraudulent, invasive of another's privacy, tortious, obscene, vulgar, pornographic, offensive, profane, contains or depicts nudity, contains or depicts sexual activity, or is otherwise inappropriate as determined by us in our sole discretion;"
      "constitutes unauthorized or unsolicited advertising, junk or bulk email (“spamming”);"
      "contains software viruses or any other computer codes, files, or programs that are designed or intended to disrupt, damage, limit or interfere with the proper function of any software, hardware, or telecommunications equipment or to damage or obtain unauthorized access to any system, data, password or other information of ours or of any third party;"
      "impersonates any person or entity, including any of our employees or representatives; or"
      "includes anyone’s identification documents or sensitive financial information.")
     "You shall not: (i) take any action that imposes or may impose (as determined by us in our sole discretion) an unreasonable or disproportionately large load on our (or our third party providers’) infrastructure; (ii) interfere or attempt to interfere with the proper working of the Services or any activities conducted on the Services; (iii) bypass, circumvent or attempt to bypass or circumvent any measures we may use to prevent or restrict access to the Services (or other accounts, computer systems or networks connected to the Services); (iv) run any form of auto-responder or “spam” on the Services; (v) use manual or automated software, devices, or other processes to “crawl” or “spider” any page of the Site; (vi) harvest or scrape any Content from the Services; or (vii) otherwise take any action in violation of our guidelines and policies."
     "You shall not: (i) take any action that imposes or may impose (as determined by us in our sole discretion) an unreasonable or disproportionately large load on our (or our third party providers’) infrastructure; (ii) interfere or attempt to interfere with the proper working of the Services or any activities conducted on the Services; (iii) bypass, circumvent or attempt to bypass or circumvent any measures we may use to prevent or restrict access to the Services (or other accounts, computer systems or networks connected to the Services); (iv) run any form of auto-responder or “spam” on the Services; (v) use manual or automated software, devices, or other processes to “crawl” or “spider” any page of the Site; (vi) harvest or scrape any Content from the Services; or (vii) otherwise take any action in violation of our guidelines and policies."
     "You shall not (directly or indirectly): (i) decipher, decompile, disassemble, reverse engineer or otherwise attempt to derive any source code or underlying ideas or algorithms of any part of the Services (including without limitation any application), except to the limited extent applicable laws specifically prohibit such restriction, (ii) modify, translate, or otherwise create derivative works of any part of the Services, or (iii) copy, rent, lease, distribute, or otherwise transfer any of the rights that you receive hereunder. You shall abide by all applicable local, state, national and international laws and regulations."
     "We also reserve the right to access, read, preserve, and disclose any information as we reasonably believe is necessary to (i) satisfy any applicable law, regulation, legal process or governmental request, (ii) enforce these Terms of Service, including investigation of potential violations hereof, (iii) detect, prevent, or otherwise address fraud, security or technical issues, (iv) respond to user support requests, or (v) protect the rights, property or safety of us, our users and the public. Again, if you have questions about how we collect, store, or use information, please consult our <PRIVACY POLICY>.")))

(def ^:private third-party-services
  (with-title "Third Party Services"
    (content "The Services may permit you to link to other websites, services or resources on the Internet, and other websites, services or resources may contain links to the Services. When you access third party resources on the Internet, you do so at your own risk. These other resources are not under our control, and you acknowledge that we are not responsible or liable for the content, functions, accuracy, legality, appropriateness or any other aspect of such websites or resources. The inclusion of any such link does not imply our endorsement or any association between us and their operators. You further acknowledge and agree that we shall not be responsible or liable, directly or indirectly, for any damage or loss caused or alleged to be caused by or in connection with the use of or reliance on any such content, goods or services available on or through any such website or resource.")))

(def ^:private payments-and-billing
  (with-title "Payments and Billing"
    (list-content
     :alphabetical
     (with-title "Paid Services"
       "Some of our Services require monetary payments now or in the future (the “Paid Services”). Please note that any payment terms presented to you in the process of using or signing up for a Paid Service are deemed part of this Agreement.")
     (with-title "Billing"
       "We use the Payment Processor to bill you through a payment account, such as a bank account or debit or credit card, linked to your Account on the Services (your “Billing Account”) for use of the Paid Services. The processing of payments will be subject to the terms, conditions and privacy policies of the Payment Processor in addition to this Agreement. We are not responsible for error by the Payment Processor. By choosing to use Paid Services, you agree to pay us, through the Payment Processor, all charges at the prices then in effect for any use of such Paid Services in accordance with the applicable payment terms and you authorize us, through the Payment Processor, to charge your chosen payment provider (your “Payment Method”). You agree to make payment using that selected Payment Method. We reserve the right to correct any errors or mistakes that it makes even if it has already requested or received payment.")
     (with-title "Payment Method"
       "The terms of your payment will be based on your Payment Method and may be determined by agreements between you and the financial institution, credit card issuer or other provider of your chosen Payment Method. If we, through the Payment Processor, do not receive payment from you, you agree to pay all amounts due on your Billing Account upon demand.")
     (with-title "Recurring Billing"
       "Some of the Paid Services may consist of an initial period, for which there is a one-time charge, followed by recurring period charges as agreed to by you. By choosing a recurring payment plan, you acknowledge that such Services have an initial and recurring payment feature and you accept responsibility for all recurring charges prior to cancellation. WE MAY SUBMIT PERIODIC CHARGES (E.G., MONTHLY) WITHOUT FURTHER AUTHORIZATION FROM YOU, UNTIL YOU PROVIDE PRIOR NOTICE (RECEIPT OF WHICH IS CONFIRMED BY US) THAT YOU HAVE TERMINATED THIS AUTHORIZATION OR WISH TO CHANGE YOUR PAYMENT METHOD. SUCH NOTICE WILL NOT AFFECT CHARGES SUBMITTED BEFORE WE REASONABLY COULD ACT. TO TERMINATE YOUR AUTHORIZATION OR CHANGE YOUR PAYMENT METHOD, GO TO <ACCOUNT SETTINGS.>")
     (with-title "Current Information Required"
       "YOU MUST PROVIDE CURRENT, COMPLETE AND ACCURATE INFORMATION FOR YOUR BILLING ACCOUNT. YOU MUST PROMPTLY UPDATE ALL INFORMATION TO KEEP YOUR BILLING ACCOUNT CURRENT, COMPLETE AND ACCURATE (SUCH AS A CHANGE IN BILLING ADDRESS, CREDIT CARD NUMBER, OR CREDIT CARD EXPIRATION DATE), AND YOU MUST PROMPTLY NOTIFY US OR OUR PAYMENT PROCESSOR IF YOUR PAYMENT METHOD IS CANCELED (E.G., FOR LOSS OR THEFT) OR IF YOU BECOME AWARE OF A POTENTIAL BREACH OF SECURITY, SUCH AS THE UNAUTHORIZED DISCLOSURE OR USE OF YOUR USER NAME OR PASSWORD. CHANGES TO SUCH INFORMATION CAN BE MADE IN <ACCOUNT SETTINGS>. IF YOU FAIL TO PROVIDE ANY OF THE FOREGOING INFORMATION, YOU AGREE THAT WE MAY CONTINUE CHARGING YOU FOR ANY USE OF PAID SERVICES UNDER YOUR BILLING ACCOUNT UNLESS YOU HAVE TERMINATED YOUR PAID SERVICES AS SET FORTH ABOVE.")
     (with-title "Change in Amount Authorized"
       "If the amount to be charged to your Billing Account varies from the amount you preauthorized (other than due to the imposition or change in the amount of state sales taxes), you have the right to receive, and we shall provide, notice of the amount to be charged and the date of the charge before the scheduled date of the transaction. Any agreement you have with your payment provider will govern your use of your Payment Method. You agree that we may accumulate charges incurred and submit them as one or more aggregate charges during or at the end of each billing cycle.")
     (with-title "Auto-Renewal for Subscription Services"
       "Unless you opt out of auto-renewal, which can be done through your <ACCOUNT SETTINGS>, any Subscription Services you have signed up for will be automatically extended for successive renewal periods of the same duration as the subscription term originally selected, at the then-current non-promotional rate. To change or resign your Subscription Services at any time, go to <ACCOUNT SETTINGS>. If you terminate a Subscription Service, you may use your subscription until the end of your then-current term; your subscription will not be renewed after your then-current term expires. However, you won't be eligible for a prorated refund of any portion of the subscription fee paid for the then-current subscription period.")
     (with-title "Reaffirmation of Authorization"
       "Your non-termination or continued use of a Paid Service reaffirms that we are authorized to charge your Payment Method for that Paid Service. We may submit those charges for payment and you will be responsible for such charges. This does not waive our right to seek payment directly from you. Your charges may be payable in advance, in arrears, per usage, or as otherwise described when you initially selected to use the Paid Service.")
     (with-title "Free Trials and Other Promotions"
       "Any free trial or other promotion that provides access to a Paid Service must be used within the specified time of the trial. You must stop using a Paid Service before the end of the trial period in order to avoid being charged for that Paid Service. If you cancel prior to the end of the trial period and are inadvertently charged for a Paid Service, please contact us at team@starcityproperties.com."))))

(def ^:private termination
  (with-title "Termination"
    (content "We may terminate your access to all or any part of the Services at any time, with or without cause, with or without notice, effective immediately, which may result in the forfeiture and destruction of all information associated with your membership. If you wish to terminate your Account, you may do so by following the instructions on the Site or through the Services. Any fees paid hereunder are non-refundable. All provisions of these Terms of Service which by their nature should survive termination shall survive termination, ownership provisions, warranty disclaimers, indemnity and limitations of liability.")))

(def ^:private warranty-disclaimer
  (with-title "Warranty Disclaimer"
    (list-content
     :alphabetical
     "We have no special relationship with or fiduciary duty to you. You acknowledge that We have no duty to take any action regarding:"
     (list-content
      :roman
      "which users gain access to the Services, including which users are approved as Starcity Members;"
      "what Content you access via the Services; or"
      "how you may interpret or use the Content.")
     "You release us from all liability for you having acquired or not acquired Content through the Services. We make no representations concerning any Content contained in or accessed through the Services, and we will not be responsible or liable for the accuracy, copyright compliance, or legality of material or Content contained in or accessed through the Services."
     "THE SERVICES AND CONTENT ARE PROVIDED “AS IS”, “AS AVAILABLE” AND WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND ANY WARRANTIES IMPLIED BY ANY COURSE OF PERFORMANCE OR USAGE OF TRADE, ALL OF WHICH ARE EXPRESSLY DISCLAIMED. WE, AND OUR DIRECTORS, EMPLOYEES, AGENTS, SUPPLIERS, PARTNERS AND CONTENT PROVIDERS DO NOT WARRANT THAT: (I) THE SERVICES WILL BE SECURE OR AVAILABLE AT ANY PARTICULAR TIME OR LOCATION; (II) ANY DEFECTS OR ERRORS WILL BE CORRECTED; (III) ANY CONTENT OR SOFTWARE AVAILABLE AT OR THROUGH THE SERVICES IS FREE OF VIRUSES OR OTHER HARMFUL COMPONENTS; OR (IV) THE RESULTS OF USING THE SERVICES WILL MEET YOUR REQUIREMENTS. YOUR USE OF THE SERVICES IS SOLELY AT YOUR OWN RISK.")))

(def ^:private idemnification
  (with-title "Idemnification"
    (content "You shall defend, indemnify, and hold harmless us, our affiliates and each of our and their respective employees, contractors, directors, suppliers and representatives from all liabilities, claims, and expenses, including reasonable attorneys’ fees, that arise from or relate to your use or misuse of, or access to, the Services, Content, violation of these Terms of Service, or infringement by you, or any third party using your Account or identity in the Services, of any intellectual property or other right of any person or entity. We reserve the right to assume the exclusive defense and control of any matter otherwise subject to indemnification by you, in which event you will assist and cooperate with us in asserting any available defenses.")))

(def ^:private disclaimer-limitation-of-liability
  (with-title "Disclaimer; Limitation of Liability"
    (paragraphs-list
     "You are solely responsible for your interactions and arrangements with other individuals, including users and Members. We make no representations or warranties as to (i) the conduct of users or Members, (ii) users’ or Members’ compatibility with current or future Members, including fellow Starcity community residents or neighbors, or (iii) any information concerning any activities or arrangements suggested by users or Members."
     "IN NO EVENT SHALL WE, NOR OUR DIRECTORS, EMPLOYEES, AGENTS, PARTNERS, SUPPLIERS OR CONTENT PROVIDERS, BE LIABLE UNDER CONTRACT, TORT, STRICT LIABILITY, NEGLIGENCE OR ANY OTHER LEGAL OR EQUITABLE THEORY WITH RESPECT TO THE SERVICES (I) FOR ANY DAMAGES ARISING OUT OF OR RELATING TO YOUR OR ANYONE ELSE’S CONDUCT IN CONNECTION WITH THE SERVICES, INCLUDING, WITHOUT LIMITATION BODILY INJURY, EMOTIONAL DISTRESS AND ANY DAMAGES RESULTING IN ANY WAY FROM COMMUNICATIONS, MEETINGS, ACTIVITIES OR ARRANGEMENTS WITH USERS, MEMBERS OR PERSONS YOU MAY OTHERWISE MEET THROUGH THE SERVICES, (II) FOR ANY LOST PROFITS, DATA LOSS, COST OF PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES, OR SPECIAL, INDIRECT, INCIDENTAL, PUNITIVE, COMPENSATORY OR CONSEQUENTIAL DAMAGES OF ANY KIND WHATSOEVER (HOWEVER ARISING), (III) FOR ANY BUGS, VIRUSES, TROJAN HORSES, OR THE LIKE (REGARDLESS OF THE SOURCE OF ORIGINATION), OR (IV) FOR ANY DIRECT DAMAGES IN EXCESS OF (IN THE AGGREGATE) OF the greater of (A) fees paid to us for the particular Services during the immediately previous three (3) month period or (B) $500.00."
     "You agree to take reasonable precautions and exercise the utmost personal care in all interactions with other users or Members or any other individual you come into contact with through the Services, particularly if you decide to meet such users, Members or individuals in person. You understand and agree that we make no guarantees, express or implied, regarding your compatibility with users, Members or other individuals you meet through the Services, including any fellow Starcity community residents or neighbors. For example, you should not, under any circumstances, provide your financial information (e.g., credit card or bank account numbers) to other individuals. We strongly advise you to use the utmost caution before sharing any personally identifiable information with others, including users and Members. We do not and cannot assure you that it is safe for you to have direct contact with any other individual that you come into contact with through the Services. If you believe that any individual is harassing you or is otherwise using personal information about you for unlawful purposes, we encourage you to first inform local law enforcement authorities, and then to contact us at team@starcityproperties.com so that we may take appropriate action.")))

(def ^:private governing-law-jurisdiction
  (with-title "Governing Law and Jurisdiction"
    (content "These Terms of Service shall be governed by and construed in accordance with the laws of the State of California, including its conflicts of law rules, and the United States of America. YOU AGREE THAT ANY DISPUTE (WHETHER OR NOT SUCH DISPUTE INVOLVES A THIRD PARTY) ARISING FROM OR RELATING TO THE SUBJECT MATTER OF THESE TERMS OF SERVICE OR YOUR RELATIONSHIP WITH US SHALL BE RESOLVED BY BINDING, INDIVIDUAL ARBITRATION UNDER THE AMERICAN ARBITRATION ASSOCIATION'S RULES FOR ARBITRATION OF CONSUMER-RELATED DISPUTES AND YOU AND WE HEREBY EXPRESSLY WAIVE TRIAL BY JURY. Neither you nor we will participate in a class action or class-wide arbitration for any claims covered by this agreement to arbitrate. YOU ARE WAIVING THE ABILITY TO PARTICIPATE AS A CLASS REPRESENTATIVE OR CLASS MEMBER ON ANY CLASS CLAIM YOU MAY HAVE AGAINST US, INCLUDING ANY RIGHT TO CLASS ARBITRATION OR ANY CONSOLIDATION OF INDIVIDUAL ARBITRATIONS. You also agree not to participate in claims brought in a private attorney general or representative capacity, or consolidated claims involving another person's account, if we are a party to the proceeding. This dispute resolution provision will be governed by the Federal Arbitration Act and not by any state law concerning arbitration. Judgment on the award rendered by the arbitrator may be entered in any court having competent jurisdiction. Any provision of applicable law notwithstanding, the arbitrator will not have authority to award damages, remedies or awards that conflict with these Terms of Service.")))

(def ^:private modification
  (with-title "Modification"
    (content "We reserve the right, in our sole discretion, to modify or replace any of these Terms of Service, or change, suspend, or discontinue the Services (including without limitation, the availability of any feature, database, or content) at any time by posting a notice on the Site or by sending you notice through the Services, via e-mail or by another appropriate means of electronic communication. We may also impose limits on certain features and services or restrict your access to parts or all of the Services without notice or liability. While we will timely provide notice of modifications, it is also your responsibility to check these Terms of Service periodically for changes. Your continued use of the Services following notification of any changes to these Terms of Service constitutes acceptance of those changes.")))

(def ^:private miscellaneous
  (with-title "Miscellaneous"
    (list-content
     :alphabetical
     (with-title "Entire Agreement and Severability"
       "These Terms of Service are the entire agreement between you and us with respect to the Services, including use of the Site, and supersede all prior or contemporaneous communications and proposals (whether oral, written or electronic) between you and us with respect to the Services. If any provision of these Terms of Service is found to be unenforceable or invalid, that provision will be limited or eliminated to the minimum extent necessary so that these Terms of Service will otherwise remain in full force and effect and enforceable. The failure of either party to exercise in any respect any right provided for herein shall not be deemed a waiver of any further rights hereunder.")
     (with-title "Force Majeure"
       "We shall not be liable for any failure to perform our obligations hereunder where such failure results from any cause beyond our reasonable control, including, without limitation, mechanical, electronic or communications failure or degradation.")
     (with-title "Assignment"
       "These Terms of Service are personal to you, and are not assignable, transferable or sublicensable by you except with our prior written consent. We may assign, transfer or delegate any of our rights and obligations hereunder without consent.")
     (with-title "Agency"
       "No agency, partnership, joint venture, or employment relationship is created as a result of these Terms of Service and neither party has any authority of any kind to bind the other in any respect.")
     (with-title "Notices"
       "Unless otherwise specified in these Term of Service, all notices under these Terms of Service will be in writing and will be deemed to have been duly given when received, if personally delivered or sent by certified or registered mail, return receipt requested; when receipt is electronically confirmed, if transmitted by facsimile or e-mail; or the day after it is sent, if sent for next day delivery by recognized overnight delivery service. Electronic notices should be sent to team@starcityproperties.com.")
     (with-title "No Waiver"
       "Our failure to enforce any part of these Terms of Service shall not constitute a waiver of our right to later enforce that or any other part of these Terms of Service. Waiver of compliance in any particular instance does not mean that we will waive comLet me pliance in the future. In order for any waiver of compliance with these Terms of Service to be binding, we must provide you with written notice of such waiver through one of our authorized representatives.")
     (with-title "Headings"
       "The section and paragraph headings in these Terms of Service are for convenience only and shall not affect their interpretation.")
     (with-title "Contact"
       "You may contact us at the following address: <u>Starcity Properties, Inc. 995 Market St., 2nd Floor, San Francisco, CA 94103</ul>"))))

(def terms
  (render-terms
   [acceptance-terms-service
    eligibility
    registration
    starcity-membership
    content-term
    rules-of-conduct
    third-party-services
    payments-and-billing
    termination
    warranty-disclaimer
    idemnification
    disclaimer-limitation-of-liability
    governing-law-jurisdiction
    modification
    miscellaneous]
   {"<PRIVACY POLICY>"   [:a {:href (format "%s/privacy" (:hostname config))} "Privacy Policy"]
    "<ACCOUNT SETTINGS>" [:a {:href (format "%s/account" (:hostname config))} "Account Settings"]}))
