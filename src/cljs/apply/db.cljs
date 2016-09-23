(ns apply.db
  ;; (:require [apply.menu.db :as menu])
  )

(def default-value
  {:prompt/current :overview/welcome})

(comment

  {:logistics [:move-in :term :communities :pet]
   :personal  [:name :phone-number :birthday :address]
   :community [:communal-living :about-you]
   :finish    [:tos-privacy :background-check :verify-income :payment]}

  ;; :logistics/move-in     {:next :logistics/term}
  ;; :logistics/term        {:prev :logistics/move-in
  ;;                         :next :logistics/communities}
  ;; :logistics/communities {:prev :logistics/term
  ;;                         :next :logistics/pet}
  ;; :logistics/pet         {:prev :logistics/term
  ;;                         :next :personal/name}
  ;; :personal/name         {:prev :logistics/pet
  ;;                         :next :personal/phone-number}

  (def ^:private prompts
    [[:logistics [:term-communities :move-in-date :pets]]
     [:personal [:full-name :phone-number :birthday :current-address]]
     [:community-fitness [:past-experience]]
     [:finish [:terms-privacy :background-check :verify-income :pay]]])


  )
