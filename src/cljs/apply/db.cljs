(ns apply.db
  (:require [apply.prompts.models :as prompts]
            [apply.logistics.db :as logistics]
            [apply.personal.db :as personal]
            [apply.community.db :as community]))

(def default-value
  (merge
   {:prompt/current    :overview/welcome
    :app/initializing  true
    :app/notifications []
    :app/properties    []}
   (prompts/syncify (merge logistics/default-value
                           personal/default-value
                           community/default-value))))
