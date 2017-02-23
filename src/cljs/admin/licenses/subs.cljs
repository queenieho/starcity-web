(ns admin.licenses.subs
  (:require [admin.licenses.db :as db]
            [re-frame.core :refer [reg-sub]]))

(reg-sub
 ::licenses
 (fn [db _]
   (db/path db)))

(reg-sub
 :licenses
 :<- [::licenses]
 (fn [db _]
   (:licenses db)))
