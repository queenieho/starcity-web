(ns admin.account.entry.security-deposit.views
  (:require [re-frame.core :refer [dispatch subscribe]]
            [clojure.string :as s]
            [starcity.log :as l]
            [starcity.components.loading :as loading]
            [cljs-time.coerce :as c]
            [cljs-time.format :as f]
            [cljs-time.core :as t]
            [starcity.components.icons :as i]))

(defn- title []
  (let [status (subscribe [:account.entry.security-deposit/status])]
    (fn []
      [:p.title.is-3
       "Security Deposit is " [:b (case @status
                                    :partial "partially paid"
                                    (name @status))]])))

(defn- overview []
  (let [security-deposit (subscribe [:account.entry/security-deposit])]
    (fn []
      [:nav.level
       [:div.level-item.has-text-centered
        [:p.heading "Amount Received"]
        [:p (str "$" (:amount-received @security-deposit))]]
       [:div.level-item.has-text-centered
        [:p.heading "Amount Required"]
        [:p (str "$" (:amount-required @security-deposit))]]
       [:div.level-item.has-text-centered
        [:p.heading "Payment Method"]
        [:p (s/upper-case (or (:payment-method @security-deposit) "n/a"))]]
       [:div.level-item.has-text-centered
        [:p.heading "Payment Type"]
        [:p (s/upper-case (or (:payment-type @security-deposit) "n/a"))]]])))

(def date-formatter
  (f/formatter "h:mma, M-d-yyyy"))

(defn- charge [{:keys [url status created amount] :as c}]
  (let [created (f/unparse date-formatter (c/from-long (* created 1000)))]
    [:div.box
     [:nav.level
      [:div.level-item
       [:p
        (case status
          "pending"   (i/hourglass)
          "succeeded" (i/check-circle)
          "failed"    (i/cross-circle)
          (i/question-circle))
        [:strong status] " payment of " [:strong (str "$" (float (/ amount 100)))]]]
      [:div.level-item.has-text-centered
       [:p.heading "View Details"]
       [:p [:a {:href url :target "_blank"} (i/stripe)]]]
      [:div.level-item.has-text-centered
       [:p.heading "Created At"]
       [:small [:b created]]]]]))

(defn- transactions []
  (let [charges (subscribe [:account.entry.security-deposit/charges])]
    (fn []
      [:div
       (when-not (empty? @charges)
         [:div
          [:h3.subtitle.is-4 "Stripe Charges"]
          (doall
           (for [c @charges]
             ^{:key (:id c)} [charge c]))])])))

(defn view []
  (let [loading (subscribe [:account.entry.security-deposit/loading?])]
    (fn []
      [:div
       (if @loading
         (loading/container "fetching security deposit")
         [:div.box.box.is-fullwidth
          [title]
          [overview]
          [transactions]])])))
