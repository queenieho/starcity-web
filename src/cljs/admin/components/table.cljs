(ns admin.components.table
  (:require [re-frame.core :refer [dispatch subscribe]]
            [clojure.string :as str]
            [starcity.log :as l]))

(defn- sortable-attrs [sort-event keys]
  (reduce
   (fn [acc k]
     (assoc acc k {:on-click #(dispatch [sort-event k])}))
   {}
   keys))

(defn- inject-sort-classes
  [sort-attrs header-key {:keys [key direction] :as sort}]
  (let [attrs   (get sort-attrs header-key {})
        active? (and (not= :none direction)
                     (= header-key key))
        init    (if (contains? sort-attrs header-key)
                  ["is-sortable"]
                  [])
        classes (cond-> init
                  active?                   (conj "is-active")
                  (and active?
                       (= :desc direction)) (conj "is-descending")
                  (and active?
                       (= :asc direction))  (conj "is-ascending"))]
    (assoc attrs :class (str/join " " classes))))

(defn header [{:keys [keys sortable-keys]} sort-sub & [titles]]
  (let [sort (subscribe [sort-sub])]
    (fn [{:keys [keys sortable-keys]} sort-sub & [titles]]
      (let [sort-attrs (sortable-attrs sort-sub sortable-keys)]
        [:thead
         [:tr
          (doall
           (for [k keys]
             (let [content (get (or titles {}) k (name k))]
               ^{:key k} [:th
                          (inject-sort-classes sort-attrs k @sort)
                          content])))]]))))
