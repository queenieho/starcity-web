(ns mars.fx.message
  (:require [re-frame.core :refer [reg-fx]]))

(def message (.-message js/window.antd))

(def loading (atom nil))

(defn- hide! [& _]
  (when-let [hide @loading]
    (hide)))

(defn- remember! [f]
  (hide!)
  (reset! loading f))

(reg-fx
 :alert/message
 (fn [{:keys [type content duration]
      :or   {type :info duration 1.5}}]
   (assert content "`:content` must be provided!")
   (remember!
    (if (= duration :indefinite)
      ((aget message (name type)) content 0)
      ((aget message (name type)) content duration)))))

(reg-fx
 :alert.message/hide
 hide!)
