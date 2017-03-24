(ns starcity.models.referral
  (:require [starcity.datomic :refer [tempid]]
            [clojure.spec :as s]
            [toolbelt.predicates :as p]
            [plumbing.core :as plumbing]))

(def sources
  "Referrals sources that are available for selection."
  ["word of mouth"
   "facebook"
   "instagram"
   "starcity member"
   "craigslist"
   "video"
   "search"
   "employer"
   "news"])

(defn tour
  "Create a tour referral."
  [source property & [account]]
  (plumbing/assoc-when
   {:db/id             (tempid)
    :referral/source   source
    :referral/from     :referral.from/tour
    :referral/tour-for (:db/id property)}
   :referral/account account))

(s/fdef tour
        :args (s/cat :source string?
                     :property p/entity?
                     :account (s/? p/entity?))
        :ret map?)
