(ns apply.routes
  (:require [accountant.core :as accountant]
            [re-frame.core :refer [dispatch]]
            [secretary.core :as secretary])
  (:require-macros [secretary.core :refer [defroute]]))

;; NOTE: See https://gist.github.com/city41/aab464ae6c112acecfe1
;; NOTE: See http://www.lispcast.com/mastering-client-side-routing-with-secretary-and-goog-history

;; =============================================================================
;; Constants

(def ^:private root "/apply")

;; =============================================================================
;; Helpers

(defn- prefix [uri]
  (str root uri))

(defn- hook-browser-navigation! []
  (accountant/configure-navigation!
   {:nav-handler  #(secretary/dispatch! %)
    :path-exists? #(secretary/locate-route %)}))

;; =============================================================================
;; API

(defn build-path
  ([] root)
  ([uri] (prefix (str "/" uri))))

(defn navigate!
  "Trigger HTML5 History navigation to `route`."
  [route]
  (accountant/navigate! route))

(declare prompt)

(defn prompt-uri
  "Produce the URI for a given `prompt-key`."
  [prompt-key]
  (let [s (namespace prompt-key)
        p (name prompt-key)]
    (cond
      ;; special case
      (= prompt-key :overview/welcome) root
      ;; not a prompt
      (nil? s)                         (build-path p)
      ;; normal case
      :otherwise                       (prompt {:section s :prompt p}))))

(defn app-routes []

  (defroute overview root []
    (dispatch [:prompt/nav :overview/welcome]))

  (defroute complete (prefix "/complete") []
    (dispatch [:complete/nav]))

  (defroute prompt (prefix "/:section/:prompt") [section prompt]
    (dispatch [:prompt/nav (keyword section prompt)]))

  (defroute (prefix "/*") []
    (accountant/navigate! root))

  ;; --------------------

  (hook-browser-navigation!)

  (accountant/dispatch-current!))
