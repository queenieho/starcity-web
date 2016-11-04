(ns starcity.models.security-deposit
  (:require [clojure.spec :as s]
            [datomic.api :as d]
            [hiccup.core :refer [html]]
            [starcity spec
             [config :refer [hostname]]
             [datomic :refer [conn tempid]]]
            [starcity.models
             [account :as account]
             [charge :as charge]
             [stripe :as stripe]
             [util :refer :all]]
            [starcity.services
             [mailgun :as mailgun]
             [slack :as slack]]
            [plumbing.core :refer [assoc-when]]
            [taoensso.timbre :as timbre]))

(defn is-security-deposit-charge?
  "Given a `charge` entity, produce the security deposit entity if there is
  one, otherwise `nil`."
  [charge]
  (qe1 '[:find ?security-deposit
         :in $ ?charge
         :where
         [?security-deposit :security-deposit/charges ?charge]]
       (d/db conn) (:db/id charge)))

(s/fdef is-security-deposit-charge?
        :args (s/cat :charge :starcity.spec/entity)
        :ret (s/or :entity :starcity.spec/entity
                   :nothing nil?))

(defn paid
  "Indicate that the security deposit is successfully paid by updating the
  charge status and setting the amount received."
  [security-deposit charge amount]
  (let [amount-dollars (int (/ amount 100))
        new-amount     (+ (get security-deposit :security-deposit/amount-received 0)
                          amount-dollars)]
    (do
      @(d/transact conn (concat
                         [{:db/id                            (:db/id security-deposit)
                           :security-deposit/amount-received amount-dollars}]
                         (charge/succeeded-tx charge)))
      (try
        (slack/rich-message "Security Deposit Paid!"
                            (format "%s has paid his/her security deposit!"
                                    (account/full-name (:security-deposit/account security-deposit)))
                            :channel "#members"
                            :opts {:color "#00d1b2"})
        (catch Exception e
          (timbre/warn e "Failed to notify after ACH payment success."))))))

(s/fdef paid
        :args (s/cat :security-deposit :starcity.spec/entity
                     :amount integer?))

(defn is-unpaid?
  [security-deposit]
  (= 0 (:security-deposit/amount-received security-deposit)))

(defn- send-ach-failure-email [account]
  (let [content (html
                 [:body
                  [:p (format "Hi %s," (:account/first-name account))]
                  [:p "Unfortunately your security deposit payment failed to go through."]
                  [:p "The most common reasons for this are insufficient funds, or incorrectly entered account credentials."]
                  [:p "Please log back in to Starcity by clicking "
                   [:a {:href (format "%s/onboarding" hostname)} "this link"]
                   " to re-enter your bank account information."]
                  mailgun/default-signature])]
    (mailgun/send-email (:account/email account)
                        "Starcity Security Deposit Payment Failure"
                        content)))

;; NOTE: Notifying logic wrapped in try-catch to avoid failing the event and
;; causing retries after the more important stuff.
(defn ach-charge-failed
  "Roll the user back to pre-ACH verification, send an email to the user to
  message the need to retry, notify admins."
  [account failure-message]
  (do
    ;; delete the stripe customer to reset user's onboarding experience.
    (stripe/delete-customer account)
    ;; notify the user that the charge has failed.
    (try
      (send-ach-failure-email account)
      ;; notify us
      (slack/rich-message "Security Deposit ACH Failure"
                          (format "%s's ACH payment has failed."
                                  (account/full-name account))
                          :channel "#members"
                          :opts {:pretext failure-message
                                 :color   "#f00"})
      (catch Exception e
        (timbre/warn e "Failed while attempting to notify after ACH charge failure.")))))

(defn- send-microdeposits-failure-email [account]
  (let [content (html
                 [:body
                  [:p (format "Hi %s," (:account/first-name account))]
                  [:p "Unfortunately we were unable to make the two small deposits to the bank account you provided &mdash; it's likely that the information provided was incorrect."]
                  [:p "Please log back in to Starcity by clicking "
                   [:a {:href (format "%s/onboarding" hostname)} "this link"]
                   " to re-enter your bank account information."]
                  mailgun/default-signature])]
    (mailgun/send-email (:account/email account)
                        "Starcity Security Deposit Payment Failure"
                        content)))

;; NOTE: Notifying logic wrapped in try-catch to avoid sending duplicitous
;; messages over one channel and not the failing one.
(defn microdeposit-verification-failed
  [{:keys [:db/id :stripe-customer/bank-account-token :stripe-customer/account]}]
  (try
    ;; notify the user that he/she needs to retry
    (send-microdeposits-failure-email account)
    ;; notify us
    (slack/rich-message "Security Deposit Verification Failure"
                        (format "%s's bank account could not be verified."
                                (account/full-name account))
                        :channel "#members"
                        :opts {:color "#f00"})
    (catch Exception e
      (timbre/warn e "Failed while attempting to notify after microdeposit failure."))))

(defn lookup [account-id]
  (one (d/db conn) :security-deposit/account account-id))

;; TODO: Does this belong in the security-deposit ns?

(def check-statuses
  #{:check.status/deposited
    :check.status/cleared
    :check.status/cancelled
    :check.status/bounced})

;; NOTE: Accepts an id to ensure that we're working with the latest version of
;; the entity
(defn- update-amount-received
  [security-deposit-id]
  (let [security-deposit (d/entity (d/db conn) security-deposit-id)
        total-checks     (reduce
                          (fn [acc check]
                            (if (= (:check/status check) :check.status/cleared)
                              (+ acc (:check/amount check))
                              acc))
                          0
                          (:security-deposit/checks security-deposit))
        total-charges    (->> (map stripe/fetch-charge (:security-deposit/charges security-deposit))
                              (reduce
                               (fn [acc {:keys [status amount]}]
                                 (if (= status "succeeded")
                                   (+ acc (/ amount 100))
                                   acc))
                               0))]
    (println total-checks total-charges)
    @(d/transact conn [{:db/id                            (:db/id security-deposit)
                        :security-deposit/amount-received (int (+ total-checks
                                                                  total-charges))}])))

(defn create-check [security-deposit {:keys [amount name number status date received-on bank]}]
  (do
    @(d/transact conn [{:db/id                   (:db/id security-deposit)
                        :security-deposit/checks [(assoc-when
                                                   {:check/amount      amount
                                                    :check/name        name
                                                    :check/number      number
                                                    :check/status      status
                                                    :check/date        date
                                                    :check/received-on received-on}
                                                   :check/bank bank)]}])
    (update-amount-received (:db/id security-deposit))))

(defn update-check [check {:keys [amount name number status date received-on bank]}]
  (do
    @(d/transact conn [(assoc-when
                        {:db/id (:db/id check)}
                        :check/amount amount
                        :check/bank bank
                        :check/name   name
                        :check/number number
                        :check/status status
                        :check/date date
                        :check/received-on received-on)])
    ;; recalculate the amount-received
    (let [security-deposit-id (:db/id (:security-deposit/_checks check))]
      (update-amount-received security-deposit-id))))

(comment

  (update-amount-received (:security-deposit/_checks (find-by (d/db conn) :check/name "Jesse Suarez")))

  )
