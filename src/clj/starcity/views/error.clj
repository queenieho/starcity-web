(ns starcity.views.error
  (:require [starcity.views.page :as p]
            [starcity.views.templates.simple :as simple]))

;; =============================================================================
;; API
;; =============================================================================

(defn error
  ([message]
   (error "Whoops! Something bad happened..." message))
  ([title message]
   (p/page
    (p/title "Error")
    (simple/danger
     (simple/body
      (simple/title title)
      (simple/subtitle message))))))
