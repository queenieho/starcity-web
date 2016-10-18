(ns apply.prompts.db)

(def default-value
  {:prompt/loading false
   :prompt/saving  false
   :prompt/current :overview/welcome
   :prompt/help    {:showing  false
                    :question ""
                    :loading  false}})
