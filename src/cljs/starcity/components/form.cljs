(ns starcity.components.form
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [register-handler
                                   path
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe]]
            [clojure.string :refer [join]]
            [starcity.util :refer [guid event-value]]))

;; =============================================================================
;; Helpers

(defn- default-class
  [class default]
  (if class
    (str class " " default)
    default))

;; (defn- install-formatter!
;;   [id pattern persistent]
;;   (js/window.Formatter.
;;    (js/document.getElementById id)
;;    #js {:pattern pattern :persistent persistent}))

;; =============================================================================
;; Select

(register-handler
 ::select.change
 (fn [app-state [_ val evt]]
   (.warn js/console "Unhandled select event!" (clj->js {:value val :event evt}))
   app-state))

(defn select
  [value options {:keys [id on-change]
                  :or   {on-change #(dispatch [::select.change %1 %2])}}]
  (let [tf (if (keyword? value) keyword identity)] ; convert output of event
                                                   ; back to keyword if it
                                                   ; started that way
    [:select.form-control
     {:id        id
      :value     value
      :on-change (fn [event] (on-change (-> event event-value tf) event))}
     (for [[n k] options]
       ^{:key k} [:option {:value k} n])]))

;; =============================================================================
;; Input

(defn- input-inner
  [_ & {:keys [placeholder type id format on-change]}]
  (reagent/create-class
   {:display-name "input-inner"
    :reagent-render
    (fn [value]
      [:input.form-control
       {:id          id
        :type        type
        :value       value
        :on-change   on-change
        :placeholder placeholder}])

    ;; TODO: Revisit form formatting in the future...formatter.js is
    ;; unmaintained and breaks with pre-existing data in the field

    ;; :component-did-mount
    ;; (fn [_]
    ;;   (when-let [{:keys [pattern persistent] :or {persistent false}} format]
    ;;     (install-formatter! id pattern persistent)))
    }))

;; Catch-all handler for inputs without a proper group
(register-handler
 ::input.change
 (fn [app-state [_ val evt]]
   (.warn js/console "Unhandled input event!" (clj->js {:value val :event evt}))
   app-state))

(defn input
  [value {:keys [placeholder type format id on-change]
          :or   {type        "text"
                 placeholder ""
                 on-change   #(dispatch [::input.change %1 %2])}
          :as   opts}]
  [input-inner value
   :placeholder placeholder
   :format format
   :id id
   :type type
   :on-change (fn [evt] (on-change (event-value evt) evt))])

;; =============================================================================
;; API

;; TODO: Abstraction?
(defn select-group
  [value options & {:keys [label class input-opts]}]
  [:div {:class (default-class class "form-group")}
   (when label [:label label])
   [select value options input-opts]])

(defn input-group
  [value & {:keys [label class input-opts] :or {input-opts {}}}]
  [:div {:class (default-class class "form-group")}
   (when label [:label label])
   [input value input-opts]])
