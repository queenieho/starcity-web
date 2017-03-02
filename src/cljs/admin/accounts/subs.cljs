(ns admin.accounts.subs
  (:require [admin.accounts.db :as db]
            [admin.accounts.check-form.subs]
            [re-frame.core :refer [reg-sub]]
            [toolbelt.core :as tb]))

(reg-sub
 ::accounts
 (fn [db _]
   (db/path db)))

(reg-sub
 ::subnav
 :<- [::accounts]
 (fn [db _]
   (:subnav db)))

(reg-sub
 :accounts
 :<- [::accounts]
 (fn [db _]
   (db/accounts db)))

;; =============================================================================
;; Search

(reg-sub
 :accounts.autocomplete/results
 :<- [::accounts]
 (fn [db _]
   (mapv
    (fn [item]
      {:text  (str (:account/name item) " <" (:account/email item) ">")
       ;; NOTE: This needs to be the email, not the id. Autocomplete
       ;; will take this value (visually, in the field), so it should
       ;; be human-legible.
       :value (:account/email item)})
    (db/autocomplete-results db))))

;; =============================================================================
;; Overview

(reg-sub
 :accounts/overview
 :<- [::accounts]
 (fn [db _]
   (:overview db)))

(reg-sub
 :accounts.overview/fetching?
 :<- [::accounts]
 (fn [db _]
   (and (db/fetching-overview? db)
        (empty? (get-in db [:overview :data])))))

(reg-sub
 :accounts.overview/members
 :<- [:accounts/overview]
 (fn [overview _]
   (:accounts/members (:data overview))))

(reg-sub
 :accounts.overview/recently-viewed
 :<- [::accounts]
 :<- [:accounts]
 (fn [[db accounts] _]
   (map
    (fn [account-id]
      (select-keys (get accounts account-id) [:db/id :account/name]))
    (:recent db))))

(reg-sub
 :accounts.overview.applicants/view
 :<- [:accounts/overview]
 (fn [overview _]
   (:applicants/view overview)))

(reg-sub
 :accounts.overview.applicants/views
 :<- [:accounts/overview]
 (fn [overview _]
   (:applicants/views overview)))

(reg-sub
 :accounts.overview/applicants
 :<- [:accounts/overview]
 :<- [:accounts.overview.applicants/view]
 (fn [[overview view] _]
   (get-in overview [:data view])))

;; =============================================================================
;; Account

(reg-sub
 :accounts/viewing
 :<- [::accounts]
 (fn [db _]
   (:viewing db)))

(reg-sub
 :account/current
 :<- [:accounts]
 :<- [:accounts/viewing]
 (fn [[accounts viewing] _]
   (get accounts viewing)))

(reg-sub
 :account/fetching?
 :<- [::accounts]
 (fn [db _]
   (db/fetching-account? db)))

(reg-sub
 :account/name
 :<- [:account/current]
 (fn [account _]
   (:account/name account)))

(reg-sub
 :account/role
 :<- [:account/current]
 (fn [account _]
   (:account/role account)))

(reg-sub
 :account/contact
 :<- [:account/current]
 (fn [account _]
   (:account/contact account)))

;; =====================================
;; Member

;; Payment Stats
(reg-sub
 :account/payment
 :<- [:account/current]
 (fn [account _]
   (:account/payment account)))

;; Security Deposit
(reg-sub
 :account/deposit
 :<- [:account/current]
 (fn [account _]
   (:account/deposit account)))

;; Member License
(reg-sub
 :account/license
 :<- [:account/current]
 (fn [account _]
   (:account/member-license account)))

;; Rent Payments
(reg-sub
 :account/rent-payments
 :<- [:account/current]
 (fn [account _]
   (:account/rent-payments account)))

;; Need to use `:account.onboarding/approval` instead of `:account/approval` due
;; to name conflict with applicant approval below
(reg-sub
 :account.onboarding/approval
 :<- [:account/current]
 (fn [account _]
   (:account/approval account)))


(reg-sub
 :account.deposit/payments
 :<- [:account/deposit]
 (fn [deposit _]
   (:deposit/payments deposit)))

(reg-sub
 :account/licenses
 :<- [:account/current]
 (fn [account _]
   (:account/member-licenses account)))

;; =====================================
;; Applicant

(reg-sub
 :account/application
 :<- [:account/current]
 (fn [account _]
   (:account/application account)))

(reg-sub
 :account.application/move-in
 :<- [:account/application]
 (fn [app _]
   (:application/move-in app)))

(reg-sub
 :account.application/fitness
 :<- [:account/application]
 (fn [application _]
   (:application/fitness application)))

(reg-sub
 :account.application/license
 :<- [:account/application]
 (fn [application _]
   (:application/license application)))

;; All approval-related data
(reg-sub
 :account/approval
 :<- [::accounts]
 (fn [db _]
   (:approval db)))

;; Is the approval modal showing?
(reg-sub
 :approval/showing?
 :<- [:account/approval]
 (fn [approval _]
   (:showing approval)))

;; Just the parameters of approval
(reg-sub
 :approval/form-data
 :<- [:account/approval]
 (fn [approval _]
   (select-keys approval [:community :license :move-in :unit])))

(reg-sub
 :approval/community
 :<- [:approval/form-data]
 (fn [data _]
   (:community data)))

(reg-sub
 :approval/license
 :<- [:approval/form-data]
 (fn [data _]
   (:license data)))

(reg-sub
 :approval/unit
 :<- [:approval/form-data]
 (fn [data _]
   (:unit data)))

;; List of units for selection -- incorporates availability date and term
(reg-sub
 :approval/units
 :<- [:account/approval]
 (fn [approval _]
   (:units approval)))

;; Should the approval action be enabled?
(reg-sub
 :approval/can-approve?
 :<- [:approval/form-data]
 (fn [{:keys [community license move-in unit]} _]
   (boolean (and community license move-in unit))))

(reg-sub
 :approval/approving?
 :<- [::accounts]
 (fn [db _]
   (db/approving? db)))

;; =============================================================================
;; Account Subnav
;; =============================================================================

;; (reg-sub
;;  :account.subnav/active
;;  :<- [:nav/current-page]
;;  :<- [::subnav]
;;  :<- [:account/role]
;;  (fn [[page {:keys [active defaults]} role] _]
;;    (case page
;;      :account (get defaults role)
;;      :account/)
;;    (println "subnav page:" page)
;;    (or active (get defaults role))))

(reg-sub
 :account.subnav/items
 :<- [::subnav]
 :<- [:account/role]
 (fn [[{:keys [items]} role] _]
   (get items role [])))
