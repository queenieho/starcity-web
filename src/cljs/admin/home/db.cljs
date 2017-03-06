(ns admin.home.db)

(def path ::home)
(def default-value
  {path {:metrics {:accounts/created     0
                   :applications/created 0
                   :controls             {}
                   :loading              false}}})

(defn metric-controls [db]
  (get-in db [:metrics :controls]))
