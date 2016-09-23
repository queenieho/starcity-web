(ns apply.logistics.models)

(defn pets-complete? [{:keys [has-pet? pet-type breed weight other]}]
  (cond
    (false? has-pet?) true
    (nil? has-pet?)   false
    :otherwise        (when pet-type
                        (case pet-type
                          "dog"   (and breed weight)
                          "other" other
                          true))))
