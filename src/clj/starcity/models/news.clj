(ns starcity.models.news
  (:require [clojure.spec :as s]
            [datomic.api :as d]
            [plumbing.core :refer [assoc-when]]
            [starcity.datomic.partition :refer [tempid]]
            [toolbelt.predicates :refer [entity?]]))

;; =============================================================================
;; Transactions

(defn create
  "Create a new news item."
  [account content & {:keys [avatar action title]}]
  (assoc-when
   {:db/id           (tempid)
    :news/account    (:db/id account)
    :news/content    content
    :news/dismissed  false
    :news/created-at (java.util.Date.)}
   :news/title title
   :news/avatar (or (:db/id avatar) [:avatar/name :system])
   :news/action action))

(s/def ::avatar entity?)
(s/def ::action keyword?)
(s/def ::title string?)

(s/fdef create
        :args (s/cat :account entity?
                     :content string?
                     :opts (s/keys* :opt-un [::avatar ::action ::title]))
        :ret (s/keys :req [:news/account :news/content :news/dismissed :news/created-at]
                     :opt [:news/title :news/avatar :news/action]))

(defn dismiss
  [news]
  {:db/id          (:db/id news)
   :news/dismissed true})

;; =====================================
;; Templates

(defn welcome
  [account]
  (create account
          "This is your primary gateway to Starcity. For now you can <b>manage your rent and payment information</b>, but there's a lot more coming; stay tuned for updates!"
          :title "Welcome to your member dashboard!"))

(def autopay-action
  :account.rent.autopay/setup)

(defn autopay
  [account]
  (create account
          "Just link your bank account and you'll never have to worry about missing a rent payment."
          :action autopay-action
          :title "Set up Automatic Rent Payments"))

;; =============================================================================
;; Queries

(defn by-action
  "Look up a news item by its `action`."
  [conn account action]
  (->> (d/q '[:find ?e .
              :in $ ?action ?account
              :where
              [?e :news/action ?action]
              [?e :news/account ?account]]
            (d/db conn) action (:db/id account))
       (d/entity (d/db conn))))
