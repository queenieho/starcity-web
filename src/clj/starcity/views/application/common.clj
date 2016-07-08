(ns starcity.views.application.common
  (:require [starcity.views.base :refer [base]]
            [starcity.models.application]
            [clojure.spec :as s]))

(declare active-step)

;; TODO: A lot of this should get moved into the model layer (?)

;; =============================================================================
;; Helpers
;; =============================================================================

(s/def ::status #{:active :complete :disabled})
(s/def ::step (s/keys :req-un [::title ::uri ::key ::num ::desc ::status]))

(def ^:private +steps+
  [{:title "Logistics"
    :uri   "/application/logistics"
    :key   :logistics
    :num   1
    :desc  "Logistics"}
   {:title "Personal Information"
    :uri   "/application/checks"
    :key   :checks
    :num   2
    :desc  "Personal Information"}
   {:title "Community Fitness"
    :uri   "/application/community"
    :key   :community
    :num   3
    :desc  "Community Fitness"}
   {:title "Submit!"
    :uri   "/application/submit"
    :key   :submit
    :num   4
    :desc  "Submit Your Application"}])

(def ^:private +steps-keyed+
  (reduce (fn [acc {:keys [key] :as step}]
            (assoc acc key step))
          {}
          +steps+))

(defn- add-statuses
  [current-steps]
  (let [active (active-step current-steps)]
    (map (fn [{:keys [key] :as step}]
           (assoc step :status (cond
                                 (= key active)      :active
                                 (current-steps key) :complete
                                 :otherwise          :disabled)))
         +steps+)))

(defn- step
  [{:keys [title uri key num status desc]}]
  [:div.col-xs-3.bs-wizard-step {:class (name status)}
   [:div.text-center.bs-wizard-stepnum (str "Step " num)]
   [:div.progress [:div.progress-bar]]
   [:a.bs-wizard-dot {:href uri}]
   [:div.bs-wizard-info.text-center desc]])

(s/fdef step :args (s/cat :step ::step))

(defn- progress-bar [current-steps]
  [:div.row.bs-wizard
   (->> (add-statuses current-steps)
        (map step))])

(s/fdef progress-bar
        :args (s/cat :current-steps :starcity.models.application/steps))

;; =============================================================================
;; API
;; =============================================================================

(defn active-step
  "Find the active step from among the current steps."
  [current-steps]
  (let [ordering (map :key +steps+)]
    (reduce (fn [acc step]
              (if (current-steps step) step acc))
            (first ordering)
            ordering)))

(s/fdef active-step
        :args (s/cat :current-steps :starcity.models.application/steps)
        :ret :starcity.models.application/step)

(defn uri-for-step
  "Get the uri for a given step."
  [step]
  (get-in +steps-keyed+ [step :uri]))

(defn title-for-step
  "Get the title for a given step."
  [step]
  (get-in +steps-keyed+ [step :title]))

(defn application
  [current-steps content & {:keys [title js json]}]
  (let [active (active-step current-steps)]
    (base
     [:div.container
      [:div.page-header
       [:h2 [:a {:href "/application"} "Member Application"]
        (if title (str " : " title) "")]]
      (progress-bar current-steps)
      content]
     :css ["application.css"]
     :js js
     :json json
     :nav-items [])))

(defn section
  [title content]
  [:li [:h3 title] [:div.question-body content]])

(def onward
  [:div.row
   [:div.form-group.col-md-2.col-md-offset-5.col-sm-4.col-sm-offset-4
    [:input.btn.btn-lg.btn-block.btn-success {:type "submit" :value "Onward"}]]])

(defn error-alerts [errors]
  [:div.row
   [:div.col-xs-10.col-xs-offset-1
    (for [e errors]
      [:div.alert.alert-danger {:role "alert"} e])]])
