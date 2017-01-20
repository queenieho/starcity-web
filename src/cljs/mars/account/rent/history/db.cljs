(ns mars.account.rent.history.db
  (:require [cljs-time.coerce :as c]))

(def path ::history)
(def default-value
  {path {:items    []
         :num-late 0
         :loading  false}})

(defn items [db]
  (:items db))

(defn set-items [db items]
  (->> (map #(-> (update % :pstart c/to-date-time)
                 (update :pend c/to-date-time)
                 (update :due c/to-date-time)
                 (update :paid c/to-date-time)) items)
       (assoc db :items)))

(defn loading? [db]
  (:loading db))

(defn toggle-loading [db]
  (update db :loading not))

(defn set-pending-ach [db payment-id]
  (let [items (items db)]
    (->> (mapv
          (fn [{:keys [id] :as item}]
            (if (= payment-id id)
              (assoc item :status "pending" :method "ach")
              item))
          items)
         (set-items db))))

(defn fetch-success [db {:keys [payments]}]
  (-> (set-items db payments)
      (assoc :loading false)))

(defn num-late-payments [db]
  (get db :num-late))
