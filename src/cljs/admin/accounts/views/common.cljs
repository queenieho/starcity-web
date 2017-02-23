(ns admin.accounts.views.common
  (:require [ant-ui.core :as a]
            [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as r]))

(defn autocomplete []
  (let [data-source (subscribe [:accounts.autocomplete/results])]
    (r/create-class
     {:component-did-mount
      (fn [this]
        (.focus (.querySelector (r/dom-node this) "input")))
      :reagent-render
      (fn []
        [a/select {:on-search     #(dispatch [:accounts.autocomplete/search %])
                   :on-select     #(dispatch [:accounts.autocomplete/select %])
                   :style         {:min-width 250}
                   :allow-clear   true
                   :filter-option false
                   :combobox      true
                   :size          "large"
                   :placeholder   "Search accounts"}
         (doall
          (for [{:keys [value text]} @data-source]
            ^{:key value} [a/select-option {:key value} text]))])})))
