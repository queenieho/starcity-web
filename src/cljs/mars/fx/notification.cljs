(ns mars.fx.notification
  (:require [re-frame.core :refer [reg-fx dispatch]]))

(def notification (.-notification js/window.antd))

(reg-fx
 :alert/notify
 (fn [{:keys [type title content duration key on-close]
      :or   {type :info duration 5}}]
   (assert (and title content) "`:title` and `:content` must be provided!")
   (let [f (aget notification (name type))]
     (f #js {:message     title
             :description content
             :duration    (if (= duration :indefinite) 0 duration)
             :key         key
             :on-close    (fn [] (dispatch on-close))}))))

(reg-fx
 :alert.notify/hide
 (fn [key]
   (.close notification key)))
