(ns mars.account.db
  (:require [mars.account.rent.db :as rent]))

(def path ::account)
(def default-value
  (merge {path {:subsection :none
                :full-name  "Josh Lehman"}}
         rent/default-value))

(defn full-name [db]
  (:full-name db))
