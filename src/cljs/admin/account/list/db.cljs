(ns admin.account.list.db)

(def root-db-key :account/list)

(def default-value
  {:header     {:keys          [:number
                                :name
                                :email
                                :phone-number]
                ;; rename to `:sortable`
                :sortable-keys [:name]}
   :list       []
   :total      0
   :sort       {:direction :desc :key :name}
   :pagination {:limit 10 :offset 0}
   :view       :all
   :views      [:all :members :applicants :pending]})
