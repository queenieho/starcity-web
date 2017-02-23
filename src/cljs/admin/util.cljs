(ns admin.util
  (:require [cljs-time.format :as f]
            [cljs-time.coerce :as c]
            [cljs.spec :as s]))

;; =============================================================================
;; Date Inputs
;; =============================================================================

(def ^:private formatter (f/formatter "yyyy-MM-dd"))

(defn date->input-format [date]
  (f/unparse formatter (c/to-date-time date)))

(s/fdef date->input-format
        :args (s/cat :date inst?)
        :ret string?)

(defn input-format->date [s]
  (c/to-date (f/parse formatter s)))

(s/fdef input-format->date
        :args (s/cat :s string?)
        :ret inst?)
