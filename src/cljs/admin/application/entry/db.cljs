(ns admin.application.entry.db)

(def root-db-key :application/entry)

(def default-value
  {:applications {}
   :active-tab   :basic-info
   :approving    false})
