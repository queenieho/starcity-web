(ns admin.account.entry.db)

(def root-db-key :account/entry)

(def default-value
  {:accounts {}
   :loading  {:account false}
   :menu     {:active :security-deposit
              :items  [["General" [:overview]]
                       ["Transactions" [:security-deposit]]]}})
