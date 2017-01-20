(ns mars.account.rent.subs
  (:require [re-frame.core :refer [reg-sub]]
            [mars.account.rent.link-account.subs]
            [mars.account.rent.history.subs]
            [mars.account.rent.db :as db]
            [starcity.log :as l]))

(reg-sub
 ::rent
 (fn [db _]
   (db/path db)))

(reg-sub
 :rent/upcoming
 :<- [::rent]
 (fn [db _]
   {:loading (db/upcoming-loading? db)
    :payment (db/upcoming-payment db)}))

(reg-sub
 :rent/bank-account
 :<- [::rent]
 (fn [db _]
   {:loading      (db/bank-account-loading? db)
    :bank-account (db/bank-account db)}))

(reg-sub
 :rent/showing-link-account?
 :<- [::rent]
 (fn [db _]
   (db/showing-link-account? db)))

(reg-sub
 :rent/autopay
 :<- [::rent]
 (fn [db _]
   {:enable-showing (db/showing-enable-autopay? db)
    :enabled        (db/autopay-enabled? db)
    :enabling       (db/enabling-autopay? db)
    :fetching       (db/fetching-autopay-status? db)}))

(reg-sub
 :rent/make-payment
 :<- [::rent]
 (fn [db _]
   {:showing (db/showing-make-payment? db)
    :payment (db/rent-payment db)
    :paying  (db/paying? db)}))

(reg-sub
 :rent/security-deposit
 :<- [::rent]
 (fn [db _]
   {:loading          (db/fetching-security-deposit? db)
    :error            (db/security-deposit-error? db)
    :confirmation     (db/security-deposit-confirmation db)
    :paying           (db/paying-security-deposit? db)
    :security-deposit (db/security-deposit db)
    :bank-linked      (db/bank-account-linked? db)}))
