(ns starcity.views.legal.privacy.content
  (:require [starcity.views.legal.render :refer :all]
            [starcity.config :refer [config]]))

;; =============================================================================
;; Content
;; =============================================================================

(def preamble
  "This is the privacy policy (“Policy”) for Starcity Properties, Inc.’s (a.k.a. “Starcity”, “we”, “our”, or “us”) online services.")

(def ^:private what-covered
  (with-title "What This Policy Covers"
    (paragraphs-list
     "This Policy pertains specifically to the way we collect, use, and store information from www.starcityproperties.com (the “Site”) and the services, features, content or applications we offer (collectively with the Site, these are the “Services”)."
     "Specifically, we put this Policy together to tell you about how we treat the personally identifiable information (“Personal Information”) that is either gathered when you use our Services or provided to us by our third-party partners that help our Services work more efficiently (“Partners”)."
     "While we seek out Partners that share our respect for your privacy, you should note that this Policy does not cover the practices of our Partners, since we do not own or control them. Even though you may engage with our Partners while interacting with our Services, such as payment processors, you should review the privacy policy of each Partner independently and carefully. Starcity does not take responsibility for the policies held by our Partners."
     "For example, before you apply to live in a Starcity community, you should also read Plaid’s privacy policy, since they are one of our Partners and the information you provide to them through our Services will be treated in accordance with their privacy policy. ")))

(def ^:private how-we-collect-information
  (with-title "How We Collect Your Information"
    (paragraphs-list
     "We receive information about you in a number of ways, including: (i) the information you provide when you create an account through the Services (your “Account”); (ii) your answers to the Starcity membership application questions (a “Membership”); (iii) your use of the Services generally; and (iv) from third party websites and services."
     "By using our Services, you consent to letting us collect, transfer, manipulate, store, disclose and otherwise use your information as described in this Policy.")))

(def ^:private what-and-why
  (with-title "What Information We Collect and Why We Collect It"
    (lead-list-content
     "We collect Personal Information and other data so we can personalize, improve and operate our Services. The following are types of information we gather from our users:"
     (list-content
      :alphabetical
      (with-title "Account Information"
        "When you create an Account, you are asked to provide some Personal Information, such as your name, password and email address. By doing so, you understand that you are sharing information with others that may be personal to you. This means your Account may not be anonymous. We may use your contact information to send you information about our Services, but only rarely when we feel such information is important. Of course, you have the option to unsubscribe from these messages through your Account settings. There may be other circumstances where we determine contacting you is necessary, such as recovering your account.")
      (with-title "Starcity Membership Application Information"
        "Prospective members who would like to apply to live in Starcity must complete an application (“Member Application”). The Member Application is broken into three parts: Logistics, Background Check, and Community Fitness.")
      (list-content
       :roman
       (with-title "Logistics"
         "In order to apply for a Starcity Membership, you have to provide Personal Information, such as your name, phone number and email address. We use the information to contact you regarding your application process for a Starcity Membership.  It may be shared with members of the Starcity community who determine community fitness (see below).")
       (with-title "Personal Information"
         "Starcity pre-qualifies applicants on the basis of criminal and financial background checks.  Certain Personal Information may be required to ascertain your background, including information you provide to us directly and information provided to our Partners. This information is <u>not</u> shared with existing members of the Starcity community.")
       (with-title "Community Fitness"
         "Since Starcity properties are designed as community housing, it is important that new members are a good cultural fit for their House. Starcity does not use information provided in this section to qualify applicants.  Rather, the existing members of the House(s) to which you apply will view your responses and determine which applicants should be interviewed."))
      (with-title "Financial Information"
        "We do not currently collect financial account information (e.g., bank checking or routing numbers; debit or credit card processing information). That information is collected and stored by our third party payment processing company (the “Payment Processor”), and use and storage of that information is governed by the Payment Processor’s applicable terms of service and privacy policy. We may, from time to time, request and receive some of your financial information from our Payment Processor for the purposes of completing transactions you have initiated through the Services, enrolling you in discount, rebate, and other programs in which you elect to participate, protecting against or identifying possible fraudulent transactions, and otherwise as needed to manage our business. Moreover, in lieu of performing a credit check during the Background Information phase of the Member Application, which can negatively impact your credit score, our Partners may provide us with a snapshot of certain portions of banking history, such as your income, their sources, and your income history for the prior year (“Financial Snapshot”). Your Financial Snapshot is stored in a secure server, kept strictly confidential, and automatically deleted after your application is processed.")
      (with-title "IP Address Information and Other Information Collected Automatically"
        "We automatically receive and record information from your web browser when you interact with our Services, including your IP address and cookie information. This information is used to fight malware and facilitate data collection concerning your interaction with the Services (e.g., what links you have clicked on). Generally, the Services automatically collect usage information, such as the number and frequency of visitors to the Site. We aggregated this data with data from other users, making it impractical to identify you personally. We and our Partners use the aggregated data to analyze how often individuals use parts of the Services so that we can improve them.")
      (list-content
       :roman
       (with-title "Email Communications"
         "We may receive a confirmation when you open an email from us. We use this confirmation to improve our customer service.")
       (with-title "Information Collected Using Cookies:"
         "Your browser likely stores cookies (tiny text files) that most websites send to you, including our Site. Cookies let our servers recognize your web browser and tell us how and when you use some of our Services. Our system does not recognize “Do Not Track” requests sent by some browsers.  However, many browsers let you block cookies, though doing so might affect your ability to utilize our Services fully. Our cookies do not, by themselves, contain Personal Information, and we do not combine the general information collected through cookies with other Personal Information to tell us who you are. As noted, however, we do use cookies to identify that your web browser has accessed aspects of the Services and may associate that information with your Account if you have one. Most browsers have an option for turning off the cookie feature, which will prevent your browser from accepting new cookies, as well as (depending on the sophistication of your browser software) allowing you to decide on acceptance of each new cookie in a variety of ways. We strongly recommend that you leave cookies active, because they enable you to take advantage the most attractive features of the Services. This Privacy Policy covers our use of cookies only and does not cover the use of cookies by third parties. We do not control when or how third parties place cookies on your computer. For example, third party websites to which a link points may set cookies on your computer.")
       (with-title "Aggregate Information"
         "We collect statistical information about how both unregistered and registered users interact with the Services (“Aggregate Information”). This statistical information is not Personal Information and cannot be tied back to you, your Account or your web browser."))))))

(def ^:private how-we-share
  (with-title "How We Share Your Information"
    (lead-list-content
     "The Services are designed to help you share information with others. As a result, some of the information generated through the Services is shared publicly or with third parties."
     (with-title "Information You Elect to Share"
       "Some features of our Service may prompt you to access our Partners’ services, such as a link to a Partners’ website from within our Site. In so do doing, you may elect to share Personal Information with Partners. Again, you should review the privacy policies of the Partners if you choose to use their services. Certain aspects of your Member Application, discussed above, may be shared with other members. Your Background Information and Financial Snapshot are never shared with with other members. We strongly advise you to use the utmost caution before sharing any Personal Information with others, including other members. You should not, under any circumstances, provide your financial information (for example, credit card or bank account numbers) to other individuals.")
     (with-title "Financial Information"
       "As described above, we collect only limited Financial Information through our Partners. Starcity do not share this information.")
     (with-title "IP Address Information"
       "We do not make information about your IP Address public. However, sometimes we share this information with our Partners and as otherwise specified in this Privacy Policy.")
     (with-title "Aggregate Information"
       "We share some Aggregate Information with our Partners. This anonymous data lets us and our Partners understand how, and how often, users interact with our Services and our Partners’ services. In addition, these third parties may share with us non-private, aggregated or otherwise non Personal Information about you that they have independently developed or acquired.")
     (with-title "Email Communications with Us"
       "As part of the Services, you may occasionally receive email and other communications from us, such as communications relating to your Account. Communications relating to your Account will only be sent for purposes important to the Services, such as password recovery.")
     (with-title "Information Shared with Our Agents"
       "We employ and contract with people and other entities that perform certain tasks on our behalf and who are under our control (our “Agents”). We may need to share Personal Information with our Agents in order for us to provide our Services. Unless we tell you differently, our Agents do not have any right to use Personal Information or other information we share with them beyond what is necessary to assist us. By using our Services, you consent to our sharing of Personal Information with our Agents.")
     (with-title "Information Disclosed Pursuant to Business Transfers"
       "At some point, we may choose to sell some or all of our assets. In these types of business transactions, user information is typically one of the transferred assets. Moreover, if we or substantially all of our assets were acquired by another company, or if we go out of business or enter bankruptcy, user information would be one of the assets that is transferred or acquired by a third party. By using our Services, you acknowledge that such transfers may occur, and that any acquirer of us or our assets may continue to use your Personal Information as set forth in this policy.")
     (with-title "Information Disclosed for Our Protection and the Protection of Others"
       "We reserve the right to access, read, preserve, and disclose any information as we reasonably believe is necessary to (i) satisfy any applicable law, regulation, legal process or governmental request, (ii) enforce this Privacy Policy and our Terms of Service, including investigation of potential violations hereof, (iii) detect, prevent, or otherwise address fraud, security or technical issues, (iv) respond to user support requests, or (v) protect our rights, property or safety or our users, Members or the public (such as when harm or violence against any person is threatened). This includes exchanging information with other companies and organizations for fraud protection, copyright infringement, and spam/malware prevention. Notwithstanding any other provision of this Privacy Policy or the Terms of Service, we reserve the right, but have no obligation, to disclose any information that you provide through the Services if in our sole opinion we suspect or have reason to suspect that the information involves a party who may be the victim of abuse in any form. Abuse may include, without limitation, elder abuse, child abuse, spousal abuse, animal abuse, neglect or domestic violence. Information may be disclosed to authorities that we, in our sole discretion, deem appropriate to handle such disclosure. Appropriate authorities may include, without limitation, law enforcement agencies, child protection agencies or court officials.")
     (with-title "Information We Share With Your Consent"
       "Except as laid out above, we will notify you when your Personal Information may be shared with third parties, and will be able to prevent the sharing of this information."))))

(def ^:private how-we-secure
  (with-title "How We Secure Your Information"
    (paragraphs-list
     "We store all of our information, including your IP address information, using industry-standard techniques. While security is always a priority, we do not guarantee or warrant these techniques will prevent unauthorized access to information about you that we store, Personal Information or otherwise."
     "Your Account information is protected by a password for your privacy and security. You need to prevent unauthorized access to your Account and Personal Information by selecting and protecting your password appropriately and limiting access to your computer and browser by signing off after you have finished accessing your Account."
     "We protect Account information to ensure that it is kept private; however, we cannot guarantee the security of any Account information. Unauthorized entry or use, hardware or software failure, and other factors, may compromise the security of user information at any time.")))

(def ^:private what-information-can-i-access
  (with-title "What Information of Mine Can I Access?"
    (paragraphs-list
     "If you are a registered user, you can access information associated with your Account by logging into the Services. Registered and unregistered users can access and delete cookies through their web browser settings."
     "California Privacy Rights : Under California Civil Code sections 1798.83-1798.84, California residents are entitled to ask us for a notice identifying the categories of personal customer information which we share with our affiliates and/or third parties for marketing purposes, and providing contact information for such affiliates and/or third parties. If you are a California resident and would like a copy of this notice, please submit a written request to the following address: <u>995 Market St., 2nd Floor, San Francisco, CA 94103</u>")))

(def ^:private how-delete
  (with-title "How Can I Delete My Account?"
    (content "You can delete your Account by emailing team@starcityproperties.com with the subject line “Terminate My Account”. If you terminate your Account, any association between your Account and information we store will no longer be accessible through your Account.")))

(def ^:private choices-regarding-information
  (with-title "What Choices Do I Have Regarding My Information"
    (list-content
     "You can limit the information we collect by using some of our Services without registering for an Account."
     "Although some of our Services require you to provide Personal Information, you can always opt not to engage our Services."
     "You can delete your Account. Please note that we will need to verify that you have the authority to delete the Account, and activity generated prior to deletion will remain stored by us and may be publicly accessible.")))

(def ^:private changes-to-privacy-policy
  (with-title "What Happens When There Are Changes to this Privacy Policy?"
    (content "We may amend this Privacy Policy occasionally. The way we use the information we collect is subject to the Privacy Policy in effect at the time such information is used. If we make changes in the way we collect or use information, we will notify you by posting an announcement on the Services or sending you an email. A user is bound by any changes to the Privacy Policy when he or she uses the Services after such changes have been first posted.")))

(def ^:private questions-concerns
  (with-title "What if I Have Questions or Concerns?"
    (content "If you have any questions or concerns regarding privacy using the Services, please send us a detailed message to team@starcityproperties.com. We will make every effort to resolve your concerns.")))

(def privacy
  (render-terms
   [what-covered
    how-we-collect-information
    what-and-why
    how-we-share
    how-we-secure
    what-information-can-i-access
    how-delete
    choices-regarding-information
    changes-to-privacy-policy
    questions-concerns]
   {}))
