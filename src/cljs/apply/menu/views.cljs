(ns apply.menu.views
  (:require [re-frame.core :refer [subscribe]]
            [apply.routes :refer [prompt-uri]]
            [reagent.core :as r]))

;; =============================================================================
;; Components
;; =============================================================================

(def ^:private prompt-labels
  {:overview/welcome "Welcome!"
   :overview/advisor "Community Advisor"

   :logistics/communities  "Choose Communities"
   :logistics/license         "Duration of Stay"
   :logistics/move-in-date "Move-in Date"
   :logistics/pets         "Pets"

   :personal/phone-number "Phone Number"
   :personal/background   "Background Check"
   :personal/income       "Income Verification"

   :community/why-starcity    "Why Starcity?"
   :community/about-you       "About You"
   :community/communal-living "Communal Living"
   })

(defn- menu-label
  ([text]
   [:p.menu-label text])
  ([text icon]
   [:p.menu-label
    [:span.icon.is-small
     [:i.fa {:class (name icon)}]]
    text]))

(def ^:private complete-blacklist
  "Set of all the prompts that shouldn't be checked for completion."
  #{:overview/welcome :overview/advisor})

(defn- menu-item* [curr-prompt this-prompt]
  (let [complete-key (keyword (str (namespace this-prompt) "." (name this-prompt)) "complete?")
        complete?    (if (complete-blacklist this-prompt) (r/atom false) (subscribe [complete-key :synced]))]
    (fn [curr-prompt this-prompt]
      [:li
       [:a {:class (str (when (= this-prompt curr-prompt) "is-active")
                        ;; (when disabled " is-disabled")
                        (when @complete? " is-complete"))
            :href  (prompt-uri this-prompt)}
        (get prompt-labels this-prompt)
        (when @complete?
          [:span.is-pulled-right.icon.is-small [:i.fa.fa-check]])]])))

(defn- menu-list [& prompts]
  (let [curr-prompt (subscribe [:prompt/current])]
    (fn [& prompts]
      [:ul.menu-list
       (doall
        (for [p prompts]
          ^{:key (str "menu-list-item-" p)} [menu-item* @curr-prompt p]))])))


;; =============================================================================
;; API
;; =============================================================================

(defn menu []
  [:aside.menu.prompt-menu
   (menu-label "Overview")
   [menu-list
    :overview/welcome
    :overview/advisor]
   (menu-label "Logistics")
   [menu-list
    :logistics/communities
    :logistics/license
    :logistics/move-in-date
    :logistics/pets]
   (menu-label "Personal Information")
   [menu-list
    :personal/phone-number
    :personal/background
    :personal/income]
   (menu-label "Community Fitness")
   [menu-list
    :community/why-starcity
    :community/about-you
    :community/communal-living]
   ;; (menu-label "Finish")
   ;; [menu-list
   ;;  {:label "Terms &amp; Privacy" :disabled true}
   ;;  {:label "Background Check" :disabled true}
   ;;  {:label "Verify Income" :disabled true}]
   ])
