(ns starcity.views.components)

;; =============================================================================
;; Forms
;; =============================================================================

(defn radio
  ([group value label]
   (radio group value label {:selected false}))
  ([group value label {:keys [selected required]}]
   (let [for (format "%s-%s" group value)]
     [:p
      [:input {:id       for
               :name     group
               :type     "radio"
               :value    value
               :checked  selected
               :required required}]
      [:label {:for for} label]])))

(defn radio-group
  [group & radios]
  (map #(apply radio group %) radios))

(defn input-field
  [& {:keys [id label placeholder name type class required attrs]
      :or   {type "text"}}]
  [:div.input-field
   [:input (merge {:id          (or id name)
                   :class       class
                   :type        type
                   :required    required
                   :placeholder placeholder
                   :name        name}
                  attrs)]
   [:label {:for (or id name)} label]])

(defn select-field
  [& {:keys [id selected options label name class choose-msg required]
      :or   {selected :none, choose-msg "Choose your option"}}]
  [:div.input-field
   [:select {:id (or id name) :name name :class class}
    [:option {:value    ""
              :disabled true
              :required required
              :selected (= selected :none)}
     choose-msg]
    (for [[value label] options]
      [:option {:value    value
                :selected (= value selected)}
       label])]
   [:label label]])

(defn checkbox
  [& {:keys [label] :as attrs}]
  [:p
   [:input (-> (dissoc attrs :label)
               (assoc :type "checkbox"))]
   [:label {:for (:id attrs)} label]])

(defn validation-group
  [& children]
  [:div.validation-group children])
