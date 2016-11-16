(ns admin.application.entry.model
  (:require-macros [hiccups.core :refer [html]])
  (:require [admin.application.entry.db :refer [root-db-key]]
            [starcity.utils.model :refer [get-in-db*
                                          assoc-in-db*]]
            [hiccups.runtime :as hiccupsrt]
            [starcity.utils :refer [find-by]]
            [starcity.log :as l]))

(def ^:private assoc-in-db (assoc-in-db* root-db-key))
(def ^:private get-in-db (get-in-db* root-db-key))

;; =============================================================================
;; Application
;; =============================================================================

(defn current-id
  "Get or set the current application id."
  ([db]
   (get-in-db db [:current-id]))
  ([db id]
   (assoc-in-db db [:current-id] id)))

(defn toggle-loading
  "Toggle whether or not the application is currently being fetched from the
  server."
  [db]
  (update-in db [root-db-key :loading] not))

(defn application
  "Retrieve or set the current application in the db."
  ([db]
   (get-in-db db [:applications (current-id db)]))
  ([db data]
   (-> (assoc-in-db db [:applications (:id data)] data)
       (current-id (:id data)))))

;; =============================================================================
;; Menu
;; =============================================================================

(defn tabs
  "Retrieve the tabs for the menu."
  [db]
  (get-in-db db [:menu :tabs]))

(def info-tab? (comp #{:information} :group))
(def action-tab? (comp #{:actions} :group))

(defn active-tab
  "Get or set the active tab."
  ([db]
   (get-in-db db [:menu :active]))
  ([db tab]
   (assoc-in-db db [:menu :active] tab)))

(def ^:private completeness-preds
  (let [ne? (comp not empty?)]
    {:move-in           (fn [{:keys [communities term move-in pet]}]
                          (and (ne? communities)
                               term
                               move-in
                               (-> pet :has-pet boolean?)))
     :community-fitness (fn [{cf :community-fitness}]
                          (let [{:keys [prior-community-housing
                                        skills
                                        why-interested
                                        free-time]} cf]
                            (and (ne? prior-community-housing)
                                 (ne? skills)
                                 (ne? why-interested)
                                 (ne? free-time))))
     :eligibility       (fn [{:keys [address income]}]
                          (and (ne? income)
                               (ne? address)))
     :approve           :approved}))

(defn determine-tab-completeness
  [db]
  (let [app (application db)]
    (->> (reduce
          (fn [acc item]
            (->> (if (info-tab? item)
                   (let [pred (get completeness-preds (:key item) (fn [_] false))]
                     (assoc item :complete (pred app)))
                   item)
                 (conj acc)))
          []
          (tabs db))
         (assoc-in-db db [:menu :tabs]))))

;; =============================================================================
;; Approval
;; =============================================================================

(defn base-rent
  "Given an `application` and `community`, determine the base rent for that
  community."
  [application community]
  (->> (:communities application)
       (find-by (comp #{community} :internal-name))
       :base-price))

(defn toggle-approving
  "Indicate that an application is currently being approved."
  [db]
  (update-in db [root-db-key :approving] not))

(defn select-community
  "Update the selected community."
  [db community]
  (assoc-in db [root-db-key :selected-community] community))

(defn security-deposit-amount
  "Set the security deposit amount."
  [db amount]
  (assoc-in db [root-db-key :deposit-amount] amount))

(defn initialize-security-deposit
  "When a new community is selected, determine the new appropriate full security
  deposit amount."
  [db community]
  (let [amount (base-rent (application db) community)]
    (security-deposit-amount db amount)))

(defn internal-name->name
  [selected communities]
  (-> (filter #(= selected (:internal-name %)) communities) first :name))

(defn status
  [db status]
  (assoc-in-db db [:applications (current-id db) :status] status))

(defn approved
  "Mark the current application as approved."
  [db]
  (status db :approved))

(defn email-subject
  "Replace the email content with `new-content`."
  [db new-content]
  (assoc-in-db db [:email-subject] new-content))

(defn email-content
  "Replace the email content with `new-content`."
  [db new-content]
  (assoc-in-db db [:email-content] new-content))

(defn- base-email-content [full-name community-name]
  (let [first-name     (first (clojure.string/split full-name #" "))
        onboarding-url (str "https://" (.. js/window -location -hostname) "/onboarding")]
    (html
     [:p (str "Hi " first-name ",")]
     [:p "We've processed your application and you've been qualified to join the Starcity community at "
      [:strong community-name] "!"]
     [:p "INSERT CUSTOM MESSAGE HERE"]
     [:p "Here are next steps:"]
     [:ol
      [:li "Schedule a tour by replying to me here with times that work for you."]
      [:li "After your tour, we'll send you a license agreement and you can pay your security deposit "
       [:a {:href onboarding-url} "here"] ". If you've laready toured with us or would like to secure your spot without a tour, you can pay now."]]
     [:p "We are excited to have you join the Starcity community and look forward to making you at home."]
     [:p "Best," [:br] "Mo"]

     [:p "Mo Sakrani" [:br]
      "516.749.0046" [:br]
      [:a {:href "mailto:mo@joinstarcity.com"} "mo@joinstarcity.com"]])))

(defn reset-email-content
  "Reset the email content to its base state upon new community selection."
  [db selected-community]
  (let [{:keys [communities name]} (application db)
        community-name            (internal-name->name selected-community communities)]
    (email-content db (base-email-content name community-name))))

;; =============================================================================
;; Rejection
;; =============================================================================

(defn toggle-rejecting [db]
  (update-in db [root-db-key :rejecting] not))
