(ns admin.application.list.db)

(def root-db-key :application/list)

(def default-value
  {:header     {:keys     [:number
                           :name
                           :email
                           :communities
                           :term
                           :move-in
                           :completed-at
                           :status]
                :sortable [:name :term :move-in :completed-at]}
   :list       []
   :total      0
   :sort       {:direction :asc :key :completed-at}
   :pagination {:limit 10 :offset 0}
   :query      ""
   :view       :submitted
   :views      [:all :in-progress :submitted :approved :rejected]})
