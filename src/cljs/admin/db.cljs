(ns admin.db
  (:require [admin.accounts.db :as accounts]
            [admin.licenses.db :as licenses]
            [admin.notes.db :as notes]
            [admin.properties.db :as properties]
            [admin.units.db :as units]))

(def nav-path ::nav)
(def default-value
  (merge {nav-path {:selected "1"
                    :items    [{:key "1" :text "Accounts" :icon "user"}
                               {:key "2" :text "Properties" :icon "home"}]
                    :route    {:page :home :params {}}}}
         accounts/default-value
         licenses/default-value
         notes/default-value
         properties/default-value
         units/default-value))

(defn page->key [page]
  (get {:accounts   "1"
        :properties "2"}
       page))

(defn key->page [key]
  (get {"1" :accounts
        "2" :properties}
       key
       :unknown))

(defn selected-menu-item
  "Get or set the selected menu item in `db`."
  ([db]
   (get db :selected))
  ([db item]
   (assoc db :selected item)))

(defn select-item
  [db page]
  (->> (cond
         (#{:account :accounts} page)          :accounts
         (#{:property :properties :unit} page) :properties
         :otherwise                            :home)
       page->key
       (selected-menu-item db)))

(defn route
  ([db]
   (:route db))
  ([db new-route]
   (assoc db :route new-route)))

(defn menu-items
  "Get the list of menu items in `db`."
  [db]
  (get db :items))

(defn current-page [db]
  (get-in db [:route :page]))
