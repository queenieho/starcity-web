(ns starcity.views.faq
  (:require [starcity.views.base :refer [base]]))

(defn- make-section
  [question answers]
  (let [answers (if (vector? answers) answers [answers])]
    {:question question
     :answers  answers}))

(def sections
  [(make-section "What is communal housing?"
                 ["Communal housing, sometimes referred to as co-living, is a new way of living inspired by the old, with community and collective experiences at its core."
                  "Starcity in particular offers welcoming, furnished spaces such as lounges, kitchens, dining areas, movie rooms, and laundry-rooms for our members to share. In these comfortable spaces, our community members come together to collaborate and exchange ideas and experiences."])
   (make-section "What about private spaces?"
                 ["Every Starcity member has a secure private room for rest and relaxation. Within our first two communities in San Francisco, each private room offers a full or queen-sized bed with fresh linens and modern furnishings."
                  "In the near future, private spaces at Starcity will have all the features and amenities of a private apartment (private bathrooms, showers and kitchenettes)."])
   (make-section "Who is this for?"
                 ["Starcity is for any individual that wants to live a community-centric lifestyle in San Francisco. Individuals that embrace diversity, resource-efficiency and a new concept of home. A home that is comfortable, uplifting, and empowering."])
   (make-section "How many people live in Starcity?"
                 ["We’re just getting started. Our first two communities have room for twenty-five people. These communities will form the DNA for larger Starcity communities set to launch in 2017."])
   (make-section "What does a Starcity membership include?"
                 ["Beyond living with a worldly and authentic collective of people, a membership includes:"
                  ["Well-designed communal spaces with furniture, appliances and more"
                   "Fully-furnished private room (queen or full-sized bed, high-end linens, nightstand, dresser, reading lamp)"
                   "Tech-enabled spaces: HDTVs and connected devices"
                   "Hi-speed WiFi"
                   "All utilities: electricity, gas, water, trash disposal"
                   "Weekly cleaning services"
                   "Resources to plan and host community events"
                   "Bicycle storage"]])
   (make-section "How much does it cost?"
                 ["Our memberships are all-inclusive &mdash; we couple rent with all of the extras described above to make your life simple. The amount you pay depends on the community you join and the term of your Membership Agreemeent. Typically:"
                  ["12-months: $2,000 per month"
                   "6-months: $2,100 per month"
                   "Month-to-month: $2,300 per month"]])
   (make-section "I want to move in. How do I secure my place in the community?"
                 ["In order to secure your spot in the community, <a href='/signup'>apply here</a>. Starcity takes care of pre-qualifying you for membership (i.e., conducting community safety and financial checks) and our community members that live in your desired home make the ultimate decision on who will join their community."])
   (make-section "What are the fees to move in?"
                 ["There is a minimal admin fee of $25 which is used to run community safety and financial screens to pre-qualify you."
                  "After pre-qualification, if our community selects you for membership, simply pay $500 towards your new member deposit to secure your room. That's it. No two month's rent, security deposit and large out of pocket before move-in."
                  "You'll pay first month's membership the day you move-in and have 30 days from move-in to pay the remainder of your new member deposit."])
   (make-section "What sort of events can I expect to happen when I move in?"
                 ["We’ll gather input from the community on types of events you’d like Starcity to organize. Early interest has asked for film nights, yoga classes, art shows, cooking classes, outings to live music as well as our own speaker series. Events will be a great way to build friendships and learn something new!"])
   (make-section "Can I host my own event?"
                 ["Yes, absolutely! Just speak to us and we can help you out. We want members to make the community their own."])
   (make-section "How do I pay my monthly membership?"
                 ["Every member at Starcity will have access to a personal online dashboard. There, you can pay your membership automatically via direct debit/ACH on the 1st of each month. This will all be set up when you sign a Membership Agreement."
                  "We want to make living in Starcity as flexible as possible for members. Upon move-in, if you come halfway through the month, you'll only pay for those days rather than the whole month."])
   (make-section "Can I decorate my room?"
                 ["Yes, you’ll be able to make yourself at home. We have a staff of expert designers and handy(wo)men who can do the work for you as part of a premium package or we can give you the tools you need to spruce up your room. Please speak to our team before putting holes in the wall yourself."])
   (make-section "Are the community spaces cleaned?"
                 ["Yes! All the community spaces are cleaned regularly by a professional cleaning service. Room cleans are available upon request as part of a premium package. We get a great rate since we'll have onsite cleaning staff each week."])
   (make-section "What is the length of a member's stay?"
                 ["We offer flexible month-to-month memberships as well as 6-month and 12-month memberships. The amount you pay is calculated based on the time you plan to stay. A longer stay means a lower monthly cost."])
   (make-section "Are there rooms available for couples and families?"
                 ["Our current rooms are ideal for individuals. We are working hard to bring larger-sized units to San Francisco that will enable couples and families to live in Starcity. We hope to launch units for couples and families in 2017."])
   (make-section "Can I have friends / significant others visit and stay?"
                 ["This is your home, so yes! While overnight guests are welcome, you're required to adhere to your community's policy on maximum stays for guests. As a matter of common courtesy, please do your best to inform your fellow community members in advance if you plan to have a guest stay for more than one night. Your guests should be respectful of the community’s shared spaces and must abide by the Community Guidelines. Ultimately, you're responsible for their actions."])
   (make-section "Is there a curfew?"
                 ["There is definitely no curfew, but we do ask you to be respectful of the people that you live with. Each Starcity Community will set their own quiet hours. The default quiet hours are from 10 pm to 6 am, Sunday through Thursday, and 12 am to 8 am on Friday and Saturday."])
   (make-section "Are pets allowed?"
                 ["Yes, dogs and cats are allowed. While we love pups of all shapes and sizes, insurance requirements restrict certain breeds and weight of dogs. Qualified service animals are always welcome. Certain communities may have a quota on the number of pets allowed as well."])
   (make-section "Is there parking on site?"
                 ["No, we don’t have any parking on-site, and we don’t encourage car ownership in San Francisco :) We believe in resource-efficiency and encourage using Zipcar, Carma or Getaround when you need a car. Members can reach out to their house concierge if they need a hand making arrangements."])
   (make-section "What happens to my membership if I want to leave early?"
                 ["Simply give us 30 days notice and we'll work diligently to find a replacement for you. Please refer to your Membership Agreement for complete details on the move-out process."])
   ])


;; =============================================================================
;; API
;; =============================================================================

(defn faq
  []
  (base
   :title "FAQ"
   :content
   [:main
    [:div.container
     [:h2 "Frequently Asked Questions"]
     [:p.flow-text "We put together this FAQ to help you better understand how Starcity works. If you're a current member and have questions about your Membership Agreement, please reach out to us at "
      [:a {:href "mailto:team@joinstarcity.com."}
       "team@joinstarcity.com"]
      "."]
     [:div.card-panel
      (for [{:keys [question answers]} sections]
        [:div.half-section
         [:h4 question]
         (for [answer answers]
           (if (vector? answer)
             [:ul.bullets
              ;; TODO: Make css class for the list items
              (for [item answer]
                [:li.flow-text-small item])]
             [:p.flow-text-small answer]))])]]]))
