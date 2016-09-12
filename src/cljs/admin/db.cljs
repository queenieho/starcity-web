(ns admin.db
  (:require [admin.applications.db :as applications]
            [admin.application.db :as application]))

(def default-value
  {:applications applications/default-value
   :application  application/default-value
   :route        :home})
