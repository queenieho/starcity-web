(ns admin.application.entry.db)

(def root-db-key :application/entry)

(def default-value
  {:applications  {}
   :approving     false
   :email-subject "Starcity: You've been qualified!"
   :menu          {:active :move-in
                   :tabs   [{:label    "Move-in"
                             :group    :information
                             :key      :move-in
                             :complete false}
                            {:label    "Community Fitness"
                             :group    :information
                             :key      :community-fitness
                             :complete false}
                            {:label    "Eligibility"
                             :group    :information
                             :key      :eligibility
                             :complete false}
                            {:label "Approve"
                             :group :actions
                             :key   :approve}]}})
