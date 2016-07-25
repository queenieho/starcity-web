(ns starcity.views.terms
  (:require [starcity.views.base :refer [base]]
            [starcity.views.terms :as terms]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defprotocol ContentItem
  "An item of content within the legal document content."
  (render [content] [content ctx] "Render a content item as Hiccup."))

(extend-type java.lang.String
  ContentItem
  (render [content]
    [:li content]))

(extend-type clojure.lang.PersistentVector
  ContentItem
  (render [[definition content]]
    [:li
     [:u definition]
     [:span ": " content]]))

(defmulti render-term-content :type)

(defmethod render-term-content :list
  [{content :content}]
  [:ol (map render content)])

(defmethod render-term-content :paragraphs
  [{content :content}]
  (list
   [:span (first content)]
   (for [p (rest content)] [:p p])))

(defmethod render-term-content :lead-list
  [{content :content}]
  (list
   [:span (first content)]
   [:ol
    (map render (rest content))]))

(defmethod render-term-content nil
  [{content :content}]
  [:span (str " " content)])

(defn- render-term [[title content]]
  [:li [:strong (str title ":")] (render-term-content content)])

;; =============================================================================
;; Content
;; =============================================================================

;; TODO: Links!
(def ^:private terms
  [


   ["Rules of Conduct"
    {:type :list
     :content ["As a condition of use, you promise not to use the Services for any purpose that is prohibited by these Terms of Service. You are responsible for all of your activity in connection with the Services."
               "You shall not (and shall not permit any third party to) either (a) take any action or (b) upload, download, post, submit or otherwise distribute or facilitate distribution of any Content on or through the Service, that:"]}]])


(def ^:private content
  [:main
   [:div.container
    [:div.center
     [:h3 "Starcity Properties, Inc"]
     [:h4 "Terms of Service"]]
    [:div.divider]
    [:p preamble]
    [:ol (map render-term terms/terms)]]])

;; =============================================================================
;; API
;; =============================================================================

(defn terms
  []
  (base :content content))
