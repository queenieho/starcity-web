(ns starcity.controllers.schedule-tour
  (:require [bouncer
             [core :as b]
             [validators :as v]]
            [clojure
             [spec :as s]
             [string :as string]]
            [datomic.api :as d]
            [net.cgrand.enlive-html :as html]
            [plumbing.core :as plumbing]
            [ring.util.response :as response]
            [starcity
             [auth :as auth]
             [datomic :refer [conn]]]
            [starcity.controllers.common :as common]
            [starcity.models
             [property :as property]
             [referral :as referral]]
            [starcity.util.validation :as validation]
            [starcity.views.base :as base]
            [toolbelt.predicates :as p]))

;; =============================================================================
;; Helpers
;; =============================================================================

(def ^:private widget-ids
  "Map of property internal names to TimeKit widget ids."
  {"2072mission" "f78e3497-5555-43de-aa75-8bc5f0e0aa80"
   "52gilbert"   "e8b4ba3d-f305-4d5d-9674-5e2a6fec4f5a"})

(defn- widget-id
  "Produce the TimeKit widget-id given then `internal-name` of a property."
  [conn internal-name]
  (when-let [property (d/entity (d/db conn) [:property/internal-name internal-name])]
    (when (property/accepting-tours? property)
      (get widget-ids internal-name))))

(defn- properties
  [conn]
  (vec
   (d/q '[:find ?n ?in ?t
          :where
          [?p :property/name ?n]
          [?p :property/internal-name ?in]
          [?p :property/tours ?t]]
        (d/db conn))))

(s/fdef properties
        :args (s/cat :conn p/conn?)
        :ret (s/* (s/spec (s/cat :name string?
                                 :internal-name string?
                                 :tours boolean?))))

(defn- validate
  [conn params]
  (b/validate
   params
   {:community      [[v/required :message "Please select a community."]
                     [v/member (set (map second (properties conn)))
                      :message "Please select a valid community."]]
    :referral       [[v/required :message "Please choose a referral method."]]
    :referral-other [[v/required
                      :message "Please elaborate upon how you found us."
                      :pre #(= "other" (:referral %))]]}))
;; =============================================================================
;; Views
;; =============================================================================

(def ^:private lead-1
  "We just need a little bit of information from you and then you can schedule your tour.")

(def ^:private lead-2
  "Thanks! Choose a time below that works for you.")

(defn- community-options [properties]
  (html/html
   (map
    (fn [[name code tours]]
      (let [attrs   (plumbing/assoc-when {:value code} :disabled (not tours))
            content (if-not tours (str name " - not touring currently") name)]
        [:option attrs content]))
    properties)))

(def ^:private referral-options
  (html/html
   (map (fn [s] [:option {:value s} (string/capitalize s)]) referral/sources)))

(html/defsnippet referral-form "templates/schedule-tour/form.html" [:form]
  [properties]
  [:select#community] (html/append (community-options properties))
  [:select#referral] (html/append referral-options))

(html/defsnippet booking-widget "templates/schedule-tour/booking.html" [:#widget]
  [widget-id]
  [:#widget] (->> (format "window.timekitBookingConfig={widgetId: '%s'}" widget-id)
                  (into [:script])
                  (html/html)
                  (html/append)))

(html/defsnippet schedule-tour "templates/schedule-tour.html" [:main]
  [properties {:keys [widget-id errors]}]
  [:p.lead] (html/content (if-not widget-id lead-1 lead-2))
  [:div.alerts] (base/maybe-errors errors)
  [:#tour-content] (html/substitute
                    (if-not widget-id
                      (referral-form properties)
                      (booking-widget widget-id))))

(defn- view [conn req & {:as opts}]
  (base/public-base req
                    :main (schedule-tour (properties conn) opts)
                    :js-bundles ["main.js" "tour.js"]))

;; =============================================================================
;; Handlers
;; =============================================================================

(defn submit!
  "Before the timekit widget is shown, a form is submitted that indicates which
  community a tour is being scheduled for and also the referral source."
  [{params :params :as req}]
  (let [vresult (validate conn params)]
    (if-let [{:keys [community referral referral-other]} (validation/valid? vresult)]
      (let [source   (if (= referral "other") referral-other referral)
            property (property/by-internal-name (d/db conn) community)]
        @(d/transact conn [(if-let [a (auth/requester req)]
                             (referral/tour source property a)
                             (referral/tour source property))])
        (response/redirect
         (format "/schedule-tour?community=%s" community)))
      (common/render-malformed
       (view conn req :errors (validation/errors vresult))))))

(defn show
  "Show the Schedule Tour page."
  [{:keys [params] :as req}]
  (->> (view conn req :widget-id (widget-id conn (:community params)))
       (common/render-ok)))
