(ns admin.accounts.db
  (:require [admin.accounts.check-form.db :as check-form]
            [toolbelt.core :as tb]
            [cljs.spec :as s]))

(def path ::accounts)
(def default-value
  (merge
   {path {:accounts     {}
          :viewing      nil
          :autocomplete []
          :recent       []

          :overview {:data             {}
                     :applicants/view  :applicants/created
                     :applicants/views [:applicants/created
                                        :applicants/active
                                        :applicants/submitted]}

          :subnav {:items {:account.role/applicant  [[:account "application"] :account/notes]
                           :account.role/member     [[:account "member"] :account/licenses :account/notes]
                           :account.role/onboarding [[:account "overview"] :account/notes]}}

          :approval {:showing false
                     :units   []}

          :loading {:account  false
                    :overview false
                    :units    false}}}
   check-form/default-value))

(defn accounts [db]
  (:accounts db))

(defn set-loading [db k v]
  (assoc-in db [:loading k] v))

;; ============================================================================
;; Autocomplete

(defn reset-autocomplete-results [db]
  (assoc db :autocomplete []))

(defn autocomplete-results
  "Retreive or fetch the autocomplete results."
  ([db]
   (get db :autocomplete))
  ([db results]
   (assoc db :autocomplete results)))

(defn select-autocomplete-result
  "Contributes information from the autocomplete result (name and email) to the
  db of accounts so that basic information about the account is visible when the
  account is viewed (presumably before all account data has been retrieved from
  server)."
  [db account-id]
  (let [results  (:autocomplete db)
        account  (tb/find-by (comp #{account-id} :db/id) results)
        existing (get-in db [:accounts account-id])]
    (assoc-in db [:accounts account-id] (merge account existing))))

(s/fdef select-autocomplete-result
        :args (s/cat :db map? :account-id integer?)
        :ret map?)

;; =============================================================================
;; Accounts Overview

(defn- set-fetching-overview [db to]
  (assoc-in db [:loading :overview] to))

(defn is-fetching-overview [db]
  (set-fetching-overview db true))

(defn fetching-overview? [db]
  (get-in db [:loading :overview]))

(defn done-fetching-overview [db overview]
  (-> (assoc-in db [:overview :data] overview)
      (set-fetching-overview false)))

(defn error-fetching-overview [db error]
  ;; TODO: Do something with error
  (tb/error error)
  (set-fetching-overview db false))

;; =============================================================================
;; Account Entry

(defn viewing-account-id
  "Get or set the id of the account that is currently being viewed."
  ([db]
   (get db :viewing))
  ([db account-id]
   (assoc db :viewing account-id)))

(s/fdef viewing-account-id
        :args (s/cat :db map? :account-id integer?)
        :ret map?)

(defn add-recently-viewed
  "Add an account to the list of recently viewed accounts."
  [db account]
  (let [account-id (if (map? account) (:db/id account) account)]
    (update db :recent (comp distinct #(conj % account-id)))))

(s/fdef add-recently-viewed
        :args (s/cat :db map? :account (s/or :map map? :integer integer?))
        :ret map?)

(defn- set-fetching-account [db to]
  (assoc-in db [:loading :account] to))

(defn done-fetching-account
  "Merge the server-side information about `account` with the current
  information we have about `account`."
  [db account]
  (-> (update-in db [:accounts (:db/id account)] merge account)
      (set-fetching-account false)
      (add-recently-viewed account)))

(defn error-fetching-account
  [db error]
  ;; TODO: do something with error
  (set-fetching-account db false))

(defn fetching-account?
  "Is an account currently being fetched from the server?"
  [db]
  (get-in db [:loading :account]))

(defn is-fetching-account
  "An account is currently being fetched."
  [db]
  (set-fetching-account db true))

;; ;; ============================================================================
;; ;; Subnav

;; (defn navigate-to
;;   "Get or set the sub-navigation."
;;   [db to]
;;   (assoc-in db [:subnav :active] to))

;; =============================================================================
;; Approval

(defn show-approval [db]
  (assoc-in db [:approval :showing] true))

(defn hide-approval [db]
  (assoc-in db [:approval :showing] false))

(defn approving? [db]
  (get-in db [:loading :approving]))

(defn is-approving [db]
  (set-loading db :approving true))

(defn done-approving [db]
  (set-loading db :approving false))

;; =====================================
;; Units

(defn fetching-units? [db]
  (get-in db [:loading :units]))

(defn is-fetching-units [db]
  (set-loading db :units true))

(defn- update-property-units
  [db units]
  (assoc-in db [:approval :units] units))

(defn done-fetching-units [db units]
  (-> (update-property-units db units)
      (set-loading :units false)))

(defn error-fetching-units [db]
  ;; TODO:
  (set-loading db :units false))
