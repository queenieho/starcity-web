(ns admin.accounts.views.overview
  (:require [admin.components.content :as c]
            [admin.components.table :as tbl]
            [admin.routes :as routes]
            [ant-ui.core :as a]
            [clojure.string :refer [capitalize]]
            [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as r]
            [toolbelt.date :as td]))

;; =============================================================================
;; Members

(def ^:private member-columns
  [(tbl/column "name"
               :render
               (fn [name record]
                 (r/as-element [:a {:href (routes/path-for :account :account-id (:id record))} name])))
   (tbl/column "property"
               :render
               #(r/as-element
                 [:a {:href (routes/path-for :property :property-id (:id %))} (:name %)]))
   (tbl/column "unit"
               :render
               (fn [u record]
                 (let [p-id (-> record :property :id)]
                   (r/as-element
                    [:a {:href (routes/path-for :unit :property-id p-id :unit-id (:id u))}
                     (:name u)]))))
   (tbl/column "rent-status"
               :title "Rent Status"
               :render #(r/as-element [:b (name %)]))])

(defn- members
  "Existing members: displays account name, property, unit and rent payment status."
  []
  (let [members (subscribe [:accounts.overview/members])]
    (fn []
      [a/card {:title "Members"}
       [a/table {:dataSource (clj->js @members)
                 :size       :small
                 :columns    member-columns}]])))

;; =============================================================================
;; Recently Viewed

(defn- recent [{:keys [db/id account/name]}]
  [:li [:a {:href (routes/path-for :account :account-id id)} name]])

(defn- recently-viewed
  "A list of recently visited accounts for quick access."
  []
  (let [recently-viewed (subscribe [:accounts.overview/recently-viewed])]
    (fn []
      [a/card {:title "Recently Viewed"}
       (if (empty? @recently-viewed)
         [:p "None."]
         [:ul
          (map-indexed
           #(with-meta (recent %2) {:key %1})
           @recently-viewed)])])))

;; =============================================================================
;; Applicants

(defn- menu-item [k]
  [a/menu-item {:class "ant-dropdown-menu-item"}
   [:a {:on-click #(dispatch [:accounts.overview.applicants/change-view k])}
    (-> k name capitalize)]])

(defn- applicants-menu []
  (let [views (subscribe [:accounts.overview.applicants/views])]
    (fn []
      [a/menu {:class "ant-dropdown-menu"
               ;; NOTE: The box-shadow property is being overridden by other ant
               ;; classes. This is a workaround until I figure out why the
               ;; override is happening. Perhaps fixed by a newer version of ant?
               :style {:box-shadow "0 1px 6px rgba(0,0,0,0.2)"}}
      (map-indexed
       #(with-meta (menu-item %2) {:key %1})
       @views)])))

(defn- columns-for-view [view]
  (let [date-key (case view
                   :applicants/created   "created-at"
                   :applicants/submitted "submitted-at"
                   "updated-at")]
    [(tbl/column "name"
                :render
                (fn [name record]
                  (r/as-element [:a {:href (routes/path-for :account :account-id (:id record))} name])))
    (tbl/column "email")
    (tbl/column date-key :render td/short-date-time)]))

(defn- applicants
  []
  (let [applicants (subscribe [:accounts.overview/applicants])
        view       (subscribe [:accounts.overview.applicants/view])]
    (fn []
      [a/card {:title "Applicants"
               :extra (r/as-element
                       [a/dropdown {:overlay (r/as-element [applicants-menu])}
                        [:a {:class "ant-dropdown-link"}
                         (str "Recently " (capitalize (name @view)) " ") [a/icon {:type "down"}]]])}
       [a/table {:dataSource (clj->js @applicants)
                 :size       :small
                 :columns    (columns-for-view @view)}]])))

;; =============================================================================
;; API

(defn content
  "Wrapper that controls the layout of content components."
  []
  (let [is-loading (subscribe [:accounts.overview/fetching?])]
    (fn []
      [c/content
       (if @is-loading
         [a/card {:loading true}]
         [:div
          [:div.columns
           [:div.column.is-two-thirds
            [members]]
           [:div.column
            [recently-viewed]]]
          [applicants]])])))
