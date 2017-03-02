(ns admin.routes
  (:require [accountant.core :as accountant]
            [bidi.bidi :as bidi]
            [re-frame.core :refer [dispatch reg-fx]]
            [toolbelt.core :as tb]))

;; Inspiration from:
;; https://github.com/PEZ/reagent-bidi-accountant-example/blob/master/src/routing_example/core.cljs

(def app-routes
  ["/admin/"
   [["" :home]
    ["accounts"
     [["" :accounts]

      [["/" :account-id] [["" :account]
                          ["/licenses" :account/licenses]
                          ["/notes" :account/notes]]]]]
    ["properties"
     [["" :properties]
      [["/" :property-id] [["" :property]
                           ["/units"
                            [[["/" :unit-id] :unit]]]]]]]]])

(defn hook-browser-navigation! []
  (accountant/configure-navigation!
   {:nav-handler  (fn [path]
                    (let [match  (bidi/match-route app-routes path)
                          page   (:handler match)
                          params (:route-params match)]
                      (dispatch [:app/route page params])))
    :path-exists? (fn [path]
                    (boolean (bidi/match-route app-routes path)))})
  (accountant/dispatch-current!))

(def path-for (partial bidi/path-for app-routes))

(reg-fx
 :route
 (fn [new-route]
   (if (vector? new-route)
     (let [[route query] new-route]
       (accountant/navigate! route query))
     (accountant/navigate! new-route))))
