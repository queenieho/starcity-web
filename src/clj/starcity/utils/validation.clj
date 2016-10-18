(ns starcity.utils.validation)

(defn errors-from
  "Extract errors from a bouncer error map."
  [[errors _]]
  (reduce (fn [acc [_ es]] (concat acc es)) [] errors))

(defn valid?
  ([vresult]
   (valid? vresult identity))
  ([[errors result] transform-after]
   (if (nil? errors)
     (transform-after result)
     false)))

(def not-valid? (comp not valid?))
