(ns mars.activity.db
  (:require [mars.routes :as routes]))

(comment
  (def sample-feed-items
    [{:id         1
      :title      "Welcome to your member portal!"
      :avatar-url "/assets/img/starcity-logo-black.png"
      :content    "This is your primary gateway to Starcity. Here you can manage your account, learn about the other members of your community, find out what's going on at home, and a lot more. Stay tuned!"}
     {:id         2
      :title      "Set up Automatic Rent Payments"
      :avatar-url "/assets/img/starcity-logo-black.png"
      :content    "Just link your bank account and you'll never have to worry about missing a rent payment."
      :action     :account.rent.autopay/setup}]))

(defn action->link [action]
  (get {:account.rent.autopay/setup (routes/account {:subsection "rent"})}
       action))

(def ^:private action->label
  {:account.rent.autopay/setup "Link My Account"})

(def ^:private action->event
  {:account.rent.autopay/setup [:rent/show-autopay]})

(defn- parse-action [item]
  (when-let [action (:action item)]
    (cond
      (string? action) (keyword action)
      (map? action)    (:key action)
      :otherwise       action)))

(defn- clientize-action [item]
  (if-let [action (parse-action item)]
    (assoc item :action {:key   action
                         :link  (action->link action)
                         :label (action->label action)
                         :event (action->event action)})
    (dissoc item :action)))

(defn- clientize-actions [feed-items]
  (mapv clientize-action feed-items))

(def path ::activity)
(def default-value
  {path {:feed {:loading false
                :items   []}}})

;; =============================================================================
;; Feed

(defn set-feed-loading
  [db to]
  (assoc-in db [:feed :loading] to))

(defn feed-loading?
  [db]
  (get-in db [:feed :loading]))

(defn set-feed [db items]
  (assoc-in db [:feed :items] (clientize-actions items)))

(defn feed [db]
  (get-in db [:feed :items]))

(defn remove-feed-item [db item-id]
  (update-in db [:feed :items]
             (fn [items] (remove (comp #{item-id} :id) items))))
