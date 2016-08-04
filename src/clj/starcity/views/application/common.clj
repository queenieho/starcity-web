(ns starcity.views.application.common
  (:require [starcity.views.base :refer [base]]
            [starcity.views.base.nav :as nav]
            [starcity.models.application]
            [clojure.spec :as s]))

;; =============================================================================
;; Helpers
;; =============================================================================

;; =============================================================================
;; Steps

(s/def ::status #{:active :complete :disabled})
(s/def ::step (s/keys :req-un [::title ::uri ::key ::num ::status]))

(def ^:private +steps+
  [{:title "Logistics"
    :uri   "/application/logistics"
    :key   :logistics
    :num   1}
   {:title "Personal Information"
    :uri   "/application/personal"
    :key   :personal
    :num   2}
   {:title "Community Fitness"
    :uri   "/application/community"
    :key   :community
    :num   3}
   {:title "Submit"
    :uri   "/application/submit"
    :key   :submit
    :num   4}])

(defn- steps-by [key]
  (reduce (fn [acc step]
            (assoc acc (get step key) step))
          {}
          +steps+))

(def ^:private +steps-by-key+ (steps-by :key))
(def ^:private +steps-by-title+ (steps-by :title))

(defn- active-step
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

(defn- add-statuses
  [current-steps]
  (let [active (active-step current-steps)]
    (map (fn [{:keys [key] :as step}]
           (assoc step :status (cond
                                 (= key active)      :active
                                 (current-steps key) :complete
                                 :otherwise          :disabled)))
         +steps+)))

(s/fdef add-statuses
        :args (s/cat :current-steps :starcity.models.application/steps)
        :ret  (s/+ ::step))

;; =============================================================================
;; Navbar

(defn- navbar-dropdown
  [current-steps]
  [:ul#steps-dropdown.dropdown-content
   (for [step (add-statuses current-steps)]
     [:li {:class (-> step :status name)}
      [:a {:href (:uri step)} (format "%s. %s" (:num step) (:title step))]])])

(defn- navbar
  [title current-steps]
  (let [current (get +steps-by-title+ title)]
    (list
     (navbar-dropdown current-steps)
     [:nav
      [:div.nav-wrapper.bone-pink.darken-2
       [:a.nav-title.truncate {:href ""} title]
       [:ul.right
        [:li
         [:a.dropdown-button {:href "" :data-activates "steps-dropdown"}
          (format "Step %s of %s" (:num current) (count +steps+))
          [:i.material-icons.right "arrow_drop_down"]]]]]])))

;; =============================================================================
;; Sections

(defn- wrap-section
  [& children]
  [:div.row
   [:div.col.s10.offset-s1
    children]])

(defn- form-content
  [sections submit-button encoding-type]
  [:form {:method "POST" :enctype encoding-type}
   (for [{:keys [title help-text content]} sections]
     [:div.section
      (wrap-section
       [:h5.section-title title]
       (when help-text
         [:div.help-text help-text])
       [:div.section-content content])])
   [:div.row
    [:div.col.s12.center
     submit-button]]])

(def ^:private default-submit-button
  [:button.btn.waves-effect.waves-light.btn-large.star-green.lighten-1
   {:type "submit"}
   "Next"])

;; =============================================================================
;; API
;; =============================================================================

(defn make-step
  ([title content]
   (make-step title nil content))
  ([title help-text content]
   {:title title :help-text help-text :content content}))

(defn step
  [title sections current-steps & {:keys [js json errors submit-button encoding-type]
                                   :or   {errors        []
                                          submit-button default-submit-button
                                          encoding-type "application/x-www-form-urlencoded"}}]
  (base
   :title "Apply"
   :content [:main#member-application
             [:div.container
              [:div.row {:style "margin-bottom: 0;"}
               (for [error errors]
                 [:div.alert.alert-error.col.s12.l10.offset-l1
                  [:p.alert-text error]])]
              [:div.row.section
               [:div.col.s12.m12.l10.offset-l1.card-panel.grey-text.text-darken-2
                (navbar title current-steps)
                (form-content sections submit-button encoding-type)]]]]
   :nav-links [nav/logout]
   :js js
   :json json))
