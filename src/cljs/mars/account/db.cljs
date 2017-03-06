(ns mars.account.db
  (:require [mars.account.rent.db :as rent]
            [mars.account.settings.db :as settings]))

(def path ::account)
(def default-value
  (merge {path {:subsection :none
                :full-name  "Josh Lehman"}}
         rent/default-value
         settings/default-value))

(defn full-name [db]
  (:full-name db))
