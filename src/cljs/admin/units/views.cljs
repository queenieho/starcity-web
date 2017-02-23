(ns admin.units.views
  (:require [admin.components.header :as h]
            [admin.components.content :as c]
            [admin.content :refer [app-content]]
            [ant-ui.core :as a]
            [re-frame.core :refer [subscribe dispatch]]
            [toolbelt.core :as tb]
            [admin.components.table :as tbl]
            [reagent.core :as r]))

(defn- editable-cell
  [value & {:keys [on-change render] :or {on-change identity
                                          render    identity}}]
  (let [editing     (r/atom false)
        local-value (r/atom value)]
    (fn [value]
      [:div.editable-cell
       (if @editing
         ;; editing = true
         [:div.editable-cell-input-wrapper
          [a/input
           {:defaultValue value
            :on-change    #(reset! local-value (.. % -target -value))
            :onPressEnter #(do
                             (reset! editing false)
                             (on-change @local-value))}]
          [a/icon {:type    "cross"
                   :class   "editable-cell-icon-cancel"
                   :onClick #(reset! editing false)}]
          [a/icon {:type    "check"
                   :class   "editable-cell-icon-check"
                   :onClick #(do
                               (reset! editing false)
                               (on-change @local-value))}]]
         ;; editing = false
         [:div.editable-cell-text-wrapper
          (if value (render value) "N/A")
          [a/icon {:type    "edit"
                   :class   "editable-cell-icon"
                   :onClick #(reset! editing true)}]])])))


(defn license-price-columns [property-id unit-id]
  [(tbl/column "term")
   (tbl/column "property-price"
               :title "Property Price"
               :render #(if % (str "$" %) "N/A"))
   (tbl/column "unit-price"
               :title "Unit Price"
               :render #(r/as-element
                         [editable-cell %
                          :on-change (fn [v record]
                                       (let [record (js->clj record :keywordize-keys true)]
                                         (dispatch [:unit.license-price/update
                                                    {:property-id      property-id
                                                     :unit-id          unit-id
                                                     :license-price-id (:id %2)
                                                     :term             (:term %2)
                                                     :price            (js/parseFloat v)}])))
                          :render (partial str "$")]))
   {:title     "Actions"
    :dataIndex "remove"
    :render    (fn [_ record]
                 (let [record (js->clj record :keywordize-keys true)]
                   (when-let [id (:id record)]
                     (r/as-element
                      [a/popconfirm
                       {:title       "Are you sure you want to delete this price for this unit?"
                        :ok-text     "Delete"
                        :cancel-text "Cancel"
                        :on-confirm  #(dispatch [:unit.license-price/remove property-id unit-id id])}
                       [:a "Remove"]]))))}])

(defn- license-prices [unit]
  (let [data-source (subscribe [:unit.viewing/license-prices])
        route       (subscribe [:nav/route])]
    (fn []
      (let [{:keys [property-id unit-id]} (:params @route)]
        [a/card {:title "License Prices"}
         [:p {:style {:margin-bottom 16}}
          "These are unit-specific overrides to the property's base rent prices. Hovering over a "
          "unit price cell will show an edit icon; click it to set the price."]
         [a/table {:dataSource (clj->js @data-source)
                   :columns    (license-price-columns property-id unit-id)}]]))))

(defn content []
  (let [is-loading (subscribe [:unit.viewing/fetching?])
        unit       (subscribe [:unit/viewing])]
    (fn []
      [c/content
       (if @is-loading
         [a/card {:loading true}]
         [:div
          [:div.columns
           [:div.column
            [license-prices @unit]]]])])))

(defmethod app-content :unit [_]
  [a/layout
   (h/header [:div])
   [content]])
