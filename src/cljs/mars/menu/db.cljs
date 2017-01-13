(ns mars.menu.db
  (:require [clojure.string :as str]
            [mars.routes :as routes]))

(defn- prefix [key prefix-key]
  (str prefix-key "." key))

(defn item
  ([key]
   (item (str/capitalize key) key))
  ([label key]
   {:type      :item
    :key       key
    :label     label}))

(defn submenu
  [key title & children]
  {:type      :submenu
   :key       key
   :title     title
   :children  (map #(update % :key prefix key) children)})

;; =============================================================================
;; API
;; =============================================================================

(def path ::menu)
(def default-value
  {path {:active     "activity"
         :open-menus []
         :items      [(item "activity")
                      (submenu "account" "Account"
                               (item "rent"))]}})

;; NOTE: Only works w/ top-level menu. Menus with more than one level of nesting
;; won't work currently.
(defn- containing-menu [key]
  (let [v (clojure.string/split key #"\.")]
    (when (> (count v) 1)
      (first v))))

(defn- reconcile-open-submenus
  "Given the currently open submenus and the new active item, produce a vector
  of the keys of the submenus that should be open.

  This is needed because the active item may be a within a submenu, and we want
  that submenu to show up as open."
  [current-open active]
  (if-let [submenu (containing-menu active)]
    (-> (conj current-open submenu) distinct vec)
    current-open))

(defn init [db open-to]
  (-> (update db :open-menus reconcile-open-submenus open-to)
      (assoc :active open-to)))

;; NOTE: enumeration is the simplest solution for now.
(defn key->route [key]
  (case key
    "activity"        (routes/activity)
    "account.rent" (routes/account {:subsection "rent"})
    (routes/activity)))

(defn set-active [db key]
  (assoc db :active key))

(defn update-open-submenus [db open]
  (assoc db :open-menus open))
