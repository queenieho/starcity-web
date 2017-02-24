(ns admin.accounts.views.entry
  (:require [admin.accounts.views.entry.application :as app]
            [admin.accounts.views.entry.license :as license]
            [admin.accounts.views.entry.rent :as rent]
            [admin.accounts.views.entry.deposit :as deposit]
            [admin.components.content :as c]
            [admin.components.level :as level]
            [ant-ui.core :as a]
            [re-frame.core :refer [dispatch subscribe]]
            [toolbelt.core :as tb]
            [clojure.string :as string]
            [toolbelt.date :as date]
            [reagent.core :as r]
            [admin.routes :as routes]))

;; =============================================================================
;; Subnav
;; =============================================================================

(defn k->s [k]
  (if-let [n (namespace k)]
    (str n "/" (name k))
    (name k)))

(defn s->k [s]
  (apply keyword (string/split s #"/")))

;; TODO: The value of the radio-buttons below needs to be a string...this makes
;; using namespaced keywords really difficult.
(defn- subnav-item [item]
  (if (vector? item)
    [a/radio-button {:value (k->s (first item))} (second item)]
    [a/radio-button {:value (k->s item)} (name item)]))

(defn- subnav []
  (let [curr  (subscribe [:account.subnav/active])
        items (subscribe [:account.subnav/items])]
    (fn []
      [a/radio-group {:value     (when-let [v @curr] (k->s v))
                      :on-change #(let [v (.. % -target -value)]
                                    (dispatch [:account.subnav/navigate-to (s->k v)]))
                      :size      :large
                      :style     {:float "right"}}
       (map-indexed #(with-meta (subnav-item %2) {:key %1}) @items)])))

;; =============================================================================
;; Content
;; =============================================================================

;; =============================================================================
;; Stats

(defn deposit-overview-items
  [{:keys [deposit/received deposit/required deposit/due-date deposit/method]}]
  (let [is-paid (and (= received required) (> received 0))]
    (if is-paid
      [(level/overview-item "Deposit Paid?" "YES")]
      [;; (level/overview-item "Deposit Total" required (partial str "$"))
       (level/overview-item "Deposit Amt. Due" (- required received) (partial str "$"))
       (level/overview-item "Payment Method" method (fnil (comp string/upper-case name) "N/A"))
       (level/overview-item "Due Date" due-date date/short-date)])))

(defn member-stats []
  (let [deposit (subscribe [:account/deposit])
        payment (subscribe [:account/payment])]
    (fn []
      (let [autopay-on (:payment/autopay @payment)]
        [a/card
         (apply level/overview
                (level/overview-item "Autopay" autopay-on #(if % "ON" "OFF"))
                (when-not autopay-on
                  (level/overview-item "Bank Linked"
                                       (:payment/bank @payment)
                                       #(if % "YES" "NO")))
                (deposit-overview-items @deposit))]))))

(defn onboarding-stats []
  (let [deposit  (subscribe [:account/deposit])
        approval (subscribe [:account.onboarding/approval])]
    (fn []
      (let [{:keys [approval/approver approval/move-in approval/term approval/unit]}
            @approval]
        [a/card
         (apply level/overview
                (level/overview-item "Approved By" approver)
                (level/overview-item "Move-in Date" move-in date/short-date)
                (level/overview-item "Term" term #(str % " months"))
                (level/overview-item "Unit" (:unit/name unit))
                (deposit-overview-items @deposit))]))))

;; =============================================================================
;; Structure

(defmulti content* identity)

(defmethod content* :default [nav]
  [a/card
   [:h2 "Nav Not Implemented: " [:strong nav]]])

(defmethod content* :member/overview [_]
  [:div
   [:div {:style {:margin-bottom 16}}
    [member-stats]]
   [a/card {:title "Rent Payments" :style {:margin-bottom 16}}
    [rent/payments]]
   [deposit/payments]])

(defmethod content* :member/licenses [_]
  [a/card {:title "Member Licenses"}
   [license/licenses]])

(defmethod content* :onboarding/overview [_]
  [:div
   [:div {:style {:margin-bottom 16}}
    [onboarding-stats]]
   [deposit/payments]])

;; NOTE: Proxy for `content*` since I cannot `subscribe` in a multimethod
(defn- applicant-content []
  (let [app (subscribe [:account/application])]
    (fn []
      (if (nil? @app)
        [a/card
         [:b "This account has not yet begun the application process."]]
        [:div
         [app/overview]
         [:div.columns {:style {:margin-top 16}}
          [:div.column.is-two-thirds
           [app/community-fitness]]
          [:div.column
           [app/eligibility]]]]))))

(defmethod content* :applicant/overview [_]
  [applicant-content])

(defn- contact []
  (let [contact (subscribe [:account/contact])
        role    (subscribe [:account/role])]
    (fn []
      (let [{:keys [:account/phone :account/email]} @contact]
        [:div.level
         [:div.level-left
          [:div.level-item
           [:strong.contact-item
            [a/icon {:type "user"}] @role]]
          (when phone
            [:div.level-item
             [:p.contact-item
              [a/icon {:type "phone"}] phone]])
          (when email
            [:div.level-item
             [:p.contact-item
              [a/icon {:type "mail"}] email]])]]))))

(defn content []
  (let [nav        (subscribe [:account.subnav/active])
        is-loading (subscribe [:account/fetching?])]
    (fn []
      [c/content
       [:div.columns
        [:div.column {:style {:padding-top 18}}
         [contact]]
        [:div.column
         [subnav]]]
       (if @is-loading
         [a/card {:loading true}]
         (content* @nav))])))
