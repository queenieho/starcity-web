(ns starcity.components.icons)

(def size-classes
  {:large   "is-large"
   :medium  "is-medium"
   :small   "is-small"})

(defn- size-class [size]
  (get size-classes size))

(defn icon
  ([name]
   (icon name :default))
  ([name size]
   [:span.icon (when-let [cls (size-class size)]
                 {:class cls})
    [:i.fa {:class (str "fa-" name)}]]))

;; Icons
(def check (partial icon "check"))
(def angle-right (partial icon "angle-right"))
(def angle-left (partial icon "angle-left"))
(def phone (partial icon "phone"))
(def user (partial icon "user"))
(def email (partial icon "envelope"))
