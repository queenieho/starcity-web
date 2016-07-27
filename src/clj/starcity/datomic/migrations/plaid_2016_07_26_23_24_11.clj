(ns starcity.datomic.migrations.plaid-2016-07-26-23-24-11)

(def migration
  [{:db/id                 #db/id[:db.part/db]
    :db/ident              :plaid/account
    :db/valueType          :db.type/ref
    :db/cardinality        :db.cardinality/one
    :db/doc                "The account that this information pertains to."
    :db.install/_attribute :db.part/db}

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :plaid/public-token
    :db/valueType          :db.type/string
    :db/cardinality        :db.cardinality/one
    :db/doc                "Public token returned by Plaid Link."
    :db.install/_attribute :db.part/db}

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :plaid/access-token
    :db/unique             :db.unique/identity
    :db/valueType          :db.type/string
    :db/cardinality        :db.cardinality/one
    :db/doc                "Access token to be used for Plaid API requests."
    :db.install/_attribute :db.part/db}

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :plaid/access-token-obtained-at
    :db/valueType          :db.type/instant
    :db/cardinality        :db.cardinality/one
    :db/doc                "Datetime that the access token was obtained at."
    :db.install/_attribute :db.part/db}

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :plaid/income
    :db/valueType          :db.type/ref
    :db/cardinality        :db.cardinality/many
    :db/isComponent        true
    :db/doc                "Income results from Plaid API."
    :db.install/_attribute :db.part/db}

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :plaid/bank-accounts
    :db/valueType          :db.type/ref
    :db/cardinality        :db.cardinality/many
    :db/isComponent        true
    :db/doc                "Bank account results from Plaid API."
    :db.install/_attribute :db.part/db}

   ;; =====================================
   ;; plaid-income

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :plaid-income/last-year
    :db/valueType          :db.type/long
    :db/cardinality        :db.cardinality/one
    :db/doc                "Last year's income."
    :db.install/_attribute :db.part/db}

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :plaid-income/last-year-pre-tax
    :db/valueType          :db.type/long
    :db/cardinality        :db.cardinality/one
    :db/doc                "Last year's pre-tax income."
    :db.install/_attribute :db.part/db}

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :plaid-income/projected-yearly
    :db/valueType          :db.type/long
    :db/cardinality        :db.cardinality/one
    :db/doc                "Projected yearly income based on current income streams."
    :db.install/_attribute :db.part/db}

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :plaid-income/projected-yearly-pre-tax
    :db/valueType          :db.type/long
    :db/cardinality        :db.cardinality/one
    :db/doc                "Projected yearly pre-tax income."
    :db.install/_attribute :db.part/db}

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :plaid-income/income-streams
    :db/valueType          :db.type/ref
    :db/cardinality        :db.cardinality/many
    :db/isComponent        true
    :db/doc                "The income streams."
    :db.install/_attribute :db.part/db}

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :plaid-income/obtained-at
    :db/valueType          :db.type/instant
    :db/cardinality        :db.cardinality/one
    :db/doc                "Datetime that the request was performed."
    :db.install/_attribute :db.part/db}

   ;; =====================================
   ;; income-stream

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :income-stream/active
    :db/valueType          :db.type/boolean
    :db/cardinality        :db.cardinality/one
    :db/doc                "Is this an active income stream?"
    :db.install/_attribute :db.part/db}

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :income-stream/confidence
    :db/valueType          :db.type/float
    :db/cardinality        :db.cardinality/one
    :db/doc                "Plaid's confidence in this stream as a source of income."
    :db.install/_attribute :db.part/db}

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :income-stream/days
    :db/valueType          :db.type/long
    :db/cardinality        :db.cardinality/one
    :db/doc                "Time in days that this was/has been an income stream."
    :db.install/_attribute :db.part/db}

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :income-stream/income
    :db/valueType          :db.type/long
    :db/cardinality        :db.cardinality/one
    :db/doc                "Monthly income from this stream."
    :db.install/_attribute :db.part/db}

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :income-stream/period
    :db/valueType          :db.type/long
    :db/cardinality        :db.cardinality/one
    :db/doc                "Interval in days that this stream recurs."
    :db.install/_attribute :db.part/db}

   ;; =====================================
   ;; bank-account

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :bank-account/available-balance
    :db/valueType          :db.type/float
    :db/cardinality        :db.cardinality/one
    :db/doc                "Available balance."
    :db.install/_attribute :db.part/db}

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :bank-account/credit-limit
    :db/valueType          :db.type/float
    :db/cardinality        :db.cardinality/one
    :db/doc                "Credit limit of this account, assuming it has :bank-account/type of 'credit'."
    :db.install/_attribute :db.part/db}

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :bank-account/current-balance
    :db/valueType          :db.type/float
    :db/cardinality        :db.cardinality/one
    :db/doc                "Current balance."
    :db.install/_attribute :db.part/db}

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :bank-account/type
    :db/valueType          :db.type/string
    :db/cardinality        :db.cardinality/one
    :db/doc                "Type of account, one of #{depository credit} TMK"
    :db.install/_attribute :db.part/db}

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :bank-account/subtype
    :db/valueType          :db.type/string
    :db/cardinality        :db.cardinality/one
    :db/doc                "Subtype of account, one of #{checking savings}"
    :db.install/_attribute :db.part/db}

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :bank-account/institution-type
    :db/valueType          :db.type/string
    :db/cardinality        :db.cardinality/one
    :db/doc                "Type of institution -- uses Plaid institution codes. See https://plaid.com/docs/api/#institutions."
    :db.install/_attribute :db.part/db}

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :bank-account/obtained-at
    :db/valueType          :db.type/instant
    :db/cardinality        :db.cardinality/one
    :db/doc                "Datetime that the information was performed."
    :db.install/_attribute :db.part/db}])

(def norms
  {:starcity/plaid-2016-07-26-23-24-11 {:txes [migration]}})
