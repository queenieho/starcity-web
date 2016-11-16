(ns admin.application.list.db)

(def root-db-key :application/list)

(def default-value
  {:header     {:keys     [:number
                           :name
                           :email
                           :phone-number
                           :communities
                           :term
                           :move-in
                           :completed-at]
                :sortable [:name :term :move-in :completed-at]}
   :list       []
   :total      0
   :sort       {:direction :desc :key :name}
   :pagination {:limit 10 :offset 0}
   :query      ""
   :view       :all
   :views      [:all :in-progress :submitted :approved :rejected]})
