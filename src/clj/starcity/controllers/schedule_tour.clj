(ns starcity.controllers.schedule-tour
  (:require [blueprints.models.property :as property]
            [blueprints.models.referral :as referral]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [clojure.spec :as s]
            [clojure.string :as string]
            [datomic.api :as d]
            [facade.core :as facade]
            [net.cgrand.enlive-html :as html]
            [ring.util.response :as response]
            [starcity.controllers.common :as common]
            [starcity.datomic :refer [conn]]
            [starcity.util.request :as req]
            [starcity.util.validation :as validation]
            [toolbelt.predicates :as p]
            [toolbelt.core :as tb]))

;; =============================================================================
;; Helpers
;; =============================================================================


(def ^:private widget-ids
  "Map of property internal names to TimeKit widget ids."
  {"2072mission" "f78e3497-5555-43de-aa75-8bc5f0e0aa80"
   "52gilbert"   "e8b4ba3d-f305-4d5d-9674-5e2a6fec4f5a"
   "6nottingham" "3d11f40d-cc10-472e-9b36-52de573ef352"})


(defn- widget-id
  "Produce the TimeKit widget-id given then `internal-name` of a property."
  [db internal-name]
  (when-let [property (property/by-internal-name db internal-name)]
    (when (property/accepting-tours? property)
      (get widget-ids internal-name))))


(defn- properties
  [db]
  (vec
   (d/q '[:find ?n ?in ?t
          :where
          [?p :property/name ?n]
          [?p :property/internal-name ?in]
          [?p :property/tours ?t]]
        db)))

(s/fdef properties
        :args (s/cat :db p/db?)
        :ret (s/* (s/spec (s/cat :name string?
                                 :internal-name string?
                                 :tours boolean?))))

(defn- validate
  [db params]
  (b/validate
   params
   {:community      [[v/required :message "Please select a community."]
                     [v/member (set (map second (properties db)))
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
      (let [attrs   (tb/assoc-when {:value code} :disabled (not tours))
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
  [:div.alerts] (facade/maybe-errors errors)
  [:#tour-content] (html/substitute
                    (if-not widget-id
                      (referral-form properties)
                      (booking-widget widget-id))))


(defn- view [db req & {:as opts}]
  (common/page req {:main        (schedule-tour (properties db) opts)
                    :css-bundles ["public.css"]
                    :js-bundles  ["main.js" "tour.js"]}))


;; =============================================================================
;; Handlers
;; =============================================================================


(defn submit!
  "Before the timekit widget is shown, a form is submitted that indicates which
  community a tour is being scheduled for and also the referral source."
  [{params :params :as req}]
  (let [vresult (validate (d/db conn) params)]
    (if-let [{:keys [community referral referral-other]} (validation/valid? vresult)]
      (let [source   (if (= referral "other") referral-other referral)
            property (property/by-internal-name (d/db conn) community)]
        @(d/transact conn [(if-let [account (req/requester (d/db conn) req)]
                             (referral/tour source property account)
                             (referral/tour source property))])
        (response/redirect
         (format "/schedule-tour?community=%s" community)))
      (common/render-malformed
       (view (d/db conn) req :errors (validation/errors vresult))))))


(defn show
  "Show the Schedule Tour page."
  [{:keys [params] :as req}]
  (->> (view (d/db conn) req
             :widget-id (when-some [c (:community params)]
                          (widget-id (d/db conn) c)))
       (common/render-ok)))
