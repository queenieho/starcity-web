(ns starcity.views.faq
  (:require [starcity.views.base :refer [base]]))

(defn- make-section
  [question answers]
  (let [answers (if (vector? answers) answers [answers])]
    {:question question
     :answers  answers}))

(def sections
  [(make-section "What is community-centric housing?"
                 ["Community-centric housing, sometimes referred to as co-living, is a new way of living inspired by the old, with community and collective experiences at its core."
                  "It offers welcoming, furnished shared spaces such as a lounge, kitchen, dining area, movie room, laundry and more. Using these welcoming spaces to bring everyone together, our community members can create collaborative and fun environments that expose everyone to new people, ideas and experiences."])
   (make-section "What about private spaces?"
                 "Every Starcity member will have a secure private room for rest and relaxation. Within our first community in SOMA, San Francisco, each private room offers a queen-sized bed with warm linens, modern furnishings and good lighting.")
   (make-section "Who is this for?"
                 ["Starcity is for any individual that wants to live a community-centric lifestyle in San Francisco. Individuals that embrace diversity, resource-efficiency and a new concept of home. A home that is comfortable, uplifting, and empowering."
                  "If you’re a native San Franciscan, it’s your new home. If you’re new to our city, it’s your home away from home."])
   (make-section "How many people live in Starcity?"
                 ["We’re just getting started. Our first community has room for six people. This community will be the DNA for larger Starcity communities set to launch in 2017."])
   (make-section "What does a Starcity membership include?"
                 ["Living with a diverse, worldly and authentic collective of people in a vibrant neighborhood. And:"
                  ["Fully-furnished communal spaces (furniture, appliances and more)"
                   "Fully-furnished private room (queen-sized bed, high-end linens, nightstand, dresser, reading lamp)"
                   "Tech-enabled spaces: HDTVs, smart appliances, etc."
                   "Laundry/dryer area"
                   "Hi-speed WiFi"
                   "All utilities: electricity, gas, water, trash disposal"
                   "Weekly cleaning services"
                   "Resources to plan and host community events"]])
   (make-section "How much does it cost?"
                 ["Our memberships are all-inclusive &mdash; we couple rent with all of the extras to make your life simple. The extras include everything mentioned in the question/answer above this one."
                  ["12-month: $1,900 per month ($1,700 rent + $200 for all the extras)"
                   "6-month: $1,950 per month ($1,750 rent + $200 for all the extras)"
                   "3-month: $1,975 per month ($1,775 rent + $200 for all the extras)"
                   "Month-to-month: $2,000 per month ($1,800 rent + $200 for all the extras)"]])
   (make-section "I want to move in. How do I secure my place in the community?"
                 ["In order to secure your spot in the community, <a href='/signup'>apply here</a>. Starcity takes care of pre-qualifying you for membership (i.e., conducting background and credit checks) and community members make the ultimate decision on who they will live with."])
   (make-section "What sort of events can I expect to happen when I move in?"
                 ["We’ll gather input from the community on types of events you’d like Starcity to organize. Early interest has asked for film nights, yoga classes, outings to live music as well as our own speaker series. Events will be a great way to build friendships and learn something new!"])
   (make-section "Can I host my own event?"
                 ["Yes, absolutely! Just speak to us and we can help you out!"])
   (make-section "How do I pay my monthly membership?"
                 ["Every member at Starcity will have access to a personal dashboard on our website. There, you can pay your membership automatically via direct debit/ACH on the 1st of the month. This will all be set up when you sign a membership agreement."
                  "We want to make living in Starcity as easy as possible for you. If you move in halfway through the month, you’ll only pay for those days rather than the whole month."])
   (make-section "Can I decorate my room?"
                 ["Yes, you’ll be able to make yourself at home. Please ask our team about how to treat the walls. We’d like to avoid any unnecessary damage. Speak to our team and we will provide the right adhesive hangers and non-marking white tack free of charge!"])
   (make-section "Are the community spaces cleaned?"
                 ["Yes! All the community spaces are cleaned once a week by a professional cleaning service. Room cleans are available upon request for a small fee. An exact schedule will be provided."])
   (make-section "What is the length of a member's stay?"
                 ["We offer flexible month-to-month memberships as well as 3, 6 and 12-month memberships. The amount you pay is directly correlated to the time you plan to stay. A longer stay means a lower monthly cost."])
   (make-section "Are there rooms available for couples and families?"
                 ["Not at this time. We are working hard to bring larger-sized units to San Francisco that will enable couples and families to live in Starcity. We hope to launch units for couples and families in 2017."])
   (make-section "Can I have friends visit and stay?"
                 ["This is your home, so yes! If they want to stay in your room, they can do so free of charge. We just ask that you do your best to inform your fellow community members in advance if you plan to have a guest stay. Your guests should be respectful of the community’s shared spaces and must abide by the community guidelines."])
   (make-section "What are the fees to move in?"
                 ["There is an admin fee of $25 which is used to run background and credit checks to pre-qualify you. After pre-qualification, if our community selects you for membership, simply pay $500 towards your new member deposit to secure your room. You'll pay first month's rent upon move-in and have 30 days from move-in to pay the remainder of your new member deposit."])
   (make-section "Is there a curfew?"
                 ["There is definitely no curfew, but we do ask you to be respectful of the people that you live with. Each Starcity Community will set their own quiet hours policy"])
   (make-section "Are pets allowed?"
                 ["Yes, dogs and cats are allowed, though there are breed and weight restrictions for the former. As always, community members make decisions based on personal preferences."])
   (make-section "Is there parking on site?"
                 ["No, we don’t have any parking on-site, and we don’t encourage car ownership in San Francisco :) We believe in resource-efficiency and encourage using Zipcar, Carma or Getaround when you need a car."])
   (make-section "What happens to my membership if I want to leave early?"
                 ["If you want to leave before your membership agreement ends, please let us know and we can find a new community member to take your room. However, you are responsible for the membership cost until we find a replacement."])
   ])


;; =============================================================================
;; API
;; =============================================================================

(defn faq
  []
  (base
   [:div.container
    [:div.page-header
     [:h1 "Frequently Asked Questions"]]
    (for [{:keys [question answers]} sections]
      [:div
       [:h3 question]
       (for [answer answers]
         (if (vector? answer)
           [:ul
            ;; TODO: Make css class for the list items
            (for [item answer] [:li {:style "font-size: 18px; font-weight: lighter;"} item])]
           [:p.lead answer]))])]))
