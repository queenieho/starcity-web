(ns starcity.controllers.schedule-tour
  (:require [selmer.parser :as selmer]
            [starcity.datomic :refer [conn]]
            [starcity.views.common :refer [public-defaults]]
            [starcity.controllers.common :as common]
            [starcity.models.referral :as referral]
            [starcity.util.validation :as validation]
            [datomic.api :as d]
            [clojure.spec :as s]
            [toolbelt.predicates :as p]
            [starcity.models.property :as property]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [ring.util.response :as response]
            [starcity.auth :as auth]))

(def ^:private render
  (partial selmer/render-file "schedule-tour.html"))

(def ^:private widget-ids
  "Map of property internal names to TimeKit widget ids."
  {"2072mission" "c126820e-5558-4df2-ab7b-1dfe6427e99c"})

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

(defn- context
  "Default context for the Schedule Tour page."
  [conn {:keys [params] :as req}]
  (-> (public-defaults req :js-bundles ["tour.js"])
      (assoc :properties (properties conn)
             :sources referral/sources)))

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
      (->> (assoc (context conn req) :errors (validation/errors vresult))
           (selmer/render-file "schedule-tour.html")
           (common/malformed)))))

(defn show
  "Show the Schedule Tour page."
  [{:keys [params] :as req}]
  (->> (assoc (context conn req) :widget-id (widget-id conn (:community params)))
       (selmer/render-file "schedule-tour.html")
       (common/ok)))
