(ns starcity.datomic.schema.plaid
  (:require [datomic-schema.schema :as s]))

(def ^{:added      "1.0.0"
       :deprecated "1.1.x"}
  schema
  (s/generate-schema
   [(s/schema
     plaid
     (s/fields
      [account :ref
       "The account that this information pertains to."]
      [public-token :string
       "Public token returned by Plaid Link."]
      [access-token :string
       "Datetime that the access token was obtained at."]
      [income :ref :many :component
       "Income results from Plaid API."]
      [bank-accounts :ref :many :component
       "Bank account results from Plaid API."]))

    (s/schema
     plaid-income
     (s/fields
      [last-year :long "Last year's income."]
      [last-year-pre-tax :long
       "Last year's pre-tax income."]
      [projected-yearly :long
       "Projected yearly income based on current income streams."]
      [projected-yearly-pre-tax :long
       "Projected yearly pre-tax income."]
      [income-streams :ref :many :component
       "The income streams."]
      [obtained-at :instant
       "Datetime that the request was performed."])

     (s/schema
      income-stream
      (s/fields
       [active :boolean
        "Is this an active income stream?"]
       [confidence :float
        "Plaid's confidence in this stream as a source of income."]
       [days :long
        "Time in days that this was/has been an income stream."]
       [income :long
        "Monthly income from this stream."]
       [period :long
        "Interval in days that this stream recurs."])

      (s/schema
       bank-account
       (s/fields
        [available-balance :float
         "Available balance."]
        [credit-limit :float
         "Credit limit of this account, assuming it has :bank-account/type of 'credit'."]
        [current-balance :float
         "Current balance."]
        [type :string
         "Type of account, one of #{depository credit} TMK"]
        [subtype :string
         "Subtype of account, one of #{checking savings}"]
        [institution-type :string
         "Type of institution -- uses Plaid institution codes. See https://plaid.com/docs/api/#institutions."]
        [obtained-at :instant
         "Datetime that the information was performed."]))))]))

(def norms
  {:starcity/add-plaid-schema
   {:txes [schema]}})
