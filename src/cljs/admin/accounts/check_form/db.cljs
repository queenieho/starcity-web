(ns admin.accounts.check-form.db
  (:require [toolbelt.core :as tb]
            [cljs.spec :as s]))

(def path ::check-form)
(def default-value
  {path {:showing    false
         :submitting false
         :params     {}
         :form       {}
         :edited     []}})

(defn check-type [params]
  (cond
    (contains? params :id)         :update
    (contains? params :deposit-id) :deposit
    (contains? params :payment-id) :rent-payment))

(defn- same-type? [db params]
  (= (check-type params) (check-type (:params db))))

(defn matching-ids? [db params]
  (let [k (get {:deposit      :deposit-id
                :rent-payment :payment-id
                :update       :id}
               (check-type params))]
    (= (get params k) (get-in db [:params k]))))

(defn- same-entity?
  [db params]
  (and (same-type? db params) (matching-ids? db params)))

(defn- should-refresh?
  "The form data should be refreshed iff the check that's being added belongs to
  a different entity, or we're in edit mode."
  [db params]
  (or (not (same-entity? db params)) (contains? params :db/id)))

(defn show [db params]
  (let [refresh (should-refresh? db params)]
    (assoc db
          :showing true
          :params params
          :form (if refresh params (:form db))
          :edited [])))

(defn hide [db]
  (assoc db :showing false))

(defn update-field [db k v]
  (let [updating (= (check-type (:params db)) :update)]
    (if updating
      (-> (assoc-in db [:form k] v)
          (update :edited conj k))
      (assoc-in db [:form k] v))))
