(ns starcity.datomic.schema.member-license.rent-alterations
  "A number of changes introduced in December of 2016 now that we're actually
  implementing a rent payment system on Stripe.

  To summarize:
  - adds indexes on existing attributes
  - renames some existing attributes
  - adds several new attributes"
  (:require [datomic-schema.schema :as s]))

(def ^:private new-attrs
  (s/generate-schema
   [(s/schema
     member-license
     (s/fields
      [active :boolean :index
       "Indicates whether or not this license is active. This is necessary
       because renewal results in creation of a new license."]

      [customer :ref :index
       "Reference to the managed :stripe-customer entity."]

      [plan-id :string :unique-identity
       "The id of the plan."]

      [subscription-id :string :unique-identity
       "The id of the subscription in Stripe that does the rent billing."]

      [unit :ref :index
       "Reference to the unit that the holder of this member license lives in."]))]))

(def ^{:added "1.2.0"} schema
  (-> [{:db/id               :member-license/commencement-date
        :db/ident            :member-license/commencement ; rename
        :db/index            true                         ; index it
        :db.alter/_attribute :db.part/db}
       {:db/id               :member-license/license
        :db/index            true
        :db.alter/_attribute :db.part/db}
       {:db/id               :member-license/price
        :db/ident            :member-license/rate
        :db/index            true
        :db.alter/_attribute :db.part/db}
       {:db/id               :member-license/end-date
        :db/ident            :member-license/ends
        :db/index            true
        :db.alter/_attribute :db.part/db}]
      (concat new-attrs)
      (vec)))
