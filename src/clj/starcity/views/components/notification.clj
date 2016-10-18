(ns starcity.views.components.notification)

(def ^:private types
  {:danger  "is-danger"
   :success "is-success"})

(defn- notification
  ([text]
   (notification text nil))
  ([text type]
   [:div.notification {:class (when type (get types type))}
    text]))

(defn danger [text]
  (notification text :danger))

(defn success [text]
  (notification text :success))
