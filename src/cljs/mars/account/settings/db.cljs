(ns mars.account.settings.db)

(def path ::settings)
(def default-value
  {path {:form-data         {:old-password ""
                             :password-1   ""
                             :password-2   ""}
         :changing-password false}})
