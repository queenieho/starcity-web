(ns starcity.seed
  (:require [blueprints.models.license :as license]
            [blueprints.seed.accounts :as accounts]
            [blueprints.seed.orders :as orders]
            [datomic.api :as d]
            [io.rkn.conformity :as cf]
            [toolbelt.core :as tb]))


(defn- rand-unit [property]
  (-> property :property/units vec rand-nth :db/id))


(defn distinct-by
  "Returns elements of xs which return unique values according to f. If multiple
  elements of xs return the same value under f, the first is returned"
  [f xs]
  (let [s (atom #{})]
    (for [x     xs
          :let  [id (f x)]
          :when (not (contains? @s id))]
      (do (swap! s conj id)
          x))))


(defn- accounts [db]
  (let [license  (license/by-term db 3)
        property (d/entity db [:property/internal-name "2072mission"])
        members  (->> (range 13)
                      (map (fn [_] (accounts/member (rand-unit property) (:db/id license))))
                      (distinct-by (comp :account/email #(tb/find-by :account/email %))))]
    (apply concat
           (accounts/member [:unit/name "52gilbert-1"] (:db/id license) :email "member@test.com")
           (accounts/admin :email "admin@test.com")
           members)))

(defn seed [conn]
  (let [db          (d/db conn)
        accounts-tx (accounts db)
        member-ids  (->> accounts-tx
                         (filter #(and (:account/email %) (= :account.role/member (:account/role %))))
                         (map (fn [m] [:account/email (:account/email m)])))]
    (->> {:seed/accounts {:txes [accounts-tx]}
          :seed/orders   {:txes     [(orders/gen-orders db member-ids)]
                          :requires [:seed/accounts]}
          :seed/avatars  {:txes [[{:db/id       (d/tempid :db.part/starcity)
                                   :avatar/name :system}]]}}
         (cf/ensure-conforms conn))))
