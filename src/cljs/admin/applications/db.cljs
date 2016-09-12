(ns admin.applications.db)

(def ^:private sample-applications
  [{:id           1
    :name         "Jocelyn Robancho"
    :email        "jmrobancho@gmail.com"
    :phone-number "(510) 418-5737"
    :properties   "SoMa"
    :term         6
    :move-in      (js/Date.)
    :completed-at (js/Date.)}])

(def default-value
  {:header-keys [:number
                 :name
                 :email
                 :phone-number
                 :properties
                 :term
                 :move-in
                 :completed-at]
   :list        []
   :sort        {:direction :none}})
