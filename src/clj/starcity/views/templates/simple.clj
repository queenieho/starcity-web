(ns starcity.views.templates.simple
  (:require [clojure.spec :as s]
            [starcity.views
             [page :as p]
             [utils :refer [errors-from]]]
            [starcity.views.components
             [button :as b]
             [form :as f]
             [hero :as h]
             [layout :as l]
             [notification :as n]]))

(s/def ::link
  (s/cat :href string? :content string?))

(s/def ::type #{:info :danger :success :primary})

(def ^:private type->class
  (comp (partial str "is-") name))

(s/fdef type->class
        :args (s/cat :type ::type)
        :ret string?)

(defn- page
  [type body & [foot]]
  (fn [req]
    (h/hero
     {:class (str "is-fullheight is-bold " (type->class type))}
     (h/head (p/navbar-inverse req))
     (h/body (body req))
     (when foot foot))))

;; =============================================================================
;; API
;; =============================================================================

;; TODO: DOCUMENTATION

(def info (partial page :info))
(def danger (partial page :danger))
(def success (partial page :success))
(def primary (partial page :primary))

(defn foot [& links]
  (h/foot
   [:nav.tabs.is-medium
    [:div.container.has-content-centered
     [:ul
      (for [[href content] links]
        [:li [:a {:href href} content]])]]]))

(s/fdef foot
        :args (s/cat :links (s/+ (s/spec ::link))))

(defn title [& content]
  [:h2.title.is-2 content])

(defn subtitle [& content]
  [:p.subtitle.is-4 content])

(defn body
  [& content]
  (fn [req]
    (l/container
     (l/columns
      ;; TODO: SASS
      ;; {:style "justify-content: center;"}
      (l/column
       {:class "is-half is-offset-one-quarter"}
       (for [error (errors-from req)]
         (n/danger error))
       (for [c content]
         (if (fn? c)
           (c req)
           c)))))))

;; TODO: SASS
(defn button [text]
  (f/control
   {:style "margin-top: 20px;"}
   (b/button {:class "is-white is-outlined is-large"} :submit text)))
