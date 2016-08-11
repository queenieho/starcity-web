module Admin.Application.Income exposing (..)

import Html exposing (..)
import Html.Attributes exposing (href, download, downloadAs)

import Json.Decode exposing (..)
import Json.Decode.Extra exposing (..)

import Material.Table as Table
import Material.Tabs as Tabs

import Admin.Application.Common as Common

-- MODEL

type alias IncomeStream =
    { active : Bool
    , confidence : Float
    , period : Int
    , income : Int
    }

type DepositoryType
    = Checking
    | Savings

type BankAccount
    -- type, current balance, available balance
    = Depository DepositoryType Float Float
    -- current balance, available balance credit limit
    | Credit Float Float Float

type alias PlaidIncome =
    { lastYear : Int
    , lastYearPreTax : Int
    , projectedYearly : Int
    , streams : Maybe (List IncomeStream)
    , accounts : Maybe (List BankAccount)
    }

type alias FileIncome =
    { name : String
    , id : Int
    }

type Income
    = Plaid PlaidIncome
    | File (List FileIncome)

-- type alias Model =
--     { }


-- VIEW


view : Income -> Html a
view data =
    let
        content =
            case data of
                Plaid plaid -> renderPlaid plaid
                File files -> renderFiles files
    in
    Common.section "Income" content

renderPlaid : PlaidIncome -> Html a
renderPlaid plaid =
    div []
        [ div []
              [ h4 [] [ text "Basic" ]
              , Common.definitionList
                  [ ("Last Year's Income", "$" ++ toString plaid.lastYear)
                  , ("Last Year's Income, Pre-Tax", "$" ++ toString plaid.lastYearPreTax)
                  , ("Projected Yearly Income", "$" ++ toString plaid.projectedYearly)
                  ]
              ]
        , Maybe.map renderIncomeStreams plaid.streams
            |> Maybe.withDefault (div [] [])
        , Maybe.map renderBankAccounts plaid.accounts
            |> Maybe.withDefault (div [] [])
        ]

renderBankAccounts : List BankAccount -> Html a
renderBankAccounts accounts =
    let
        textForDepositoryType t =
            case t of
                Checking -> "Checking"
                Savings -> "Savings"

        row t st curr avail lim =
            Table.tr []
                [ Table.td [] [ text t ]
                , Table.td [] [ text st ]
                , Table.td [] [ text curr ]
                , Table.td [] [ text avail ]
                , Table.td [] [ text lim ]
                ]

        accountRow account =
            case account of
                Depository t curr avail ->
                    row "Depository"
                        (textForDepositoryType t)
                        ("$" ++ toString curr)
                        ("$" ++ toString avail)
                        "N/A"

                Credit curr avail lim ->
                    row "Credit"
                        "N/A"
                        ("$" ++ toString curr)
                        ("$" ++ toString avail)
                        ("$" ++ toString lim)

    in
    div []
        [ h4 [] [ text "Bank Accounts" ]
        , Table.table []
            [ Table.thead []
                  [ Table.tr []
                        [ Table.th [] [ text "Account Type" ]
                        , Table.th [] [ text "Account Subtype" ]
                        , Table.th [] [ text "Current Balance" ]
                        , Table.th [] [ text "Available Balance" ]
                        , Table.th [] [ text "Credit LImit" ]
                        ]
                  ]
            , Table.tbody []
                <| List.map accountRow accounts
            ]
        ]


renderIncomeStreams : List IncomeStream -> Html a
renderIncomeStreams streams =
    div []
        [ h4 [] [ text "Income Streams" ]
        , incomeStreamTable streams
        ]

incomeStreamTable : List IncomeStream -> Html a
incomeStreamTable streams =
    let
        incomeStreamRow stream =
            Table.tr []
                [ Table.td [] [ text <| toString stream.active ]
                , Table.td [] [ text <| toString stream.confidence ]
                , Table.td [] [ text <| toString stream.period ++ " days" ]
                , Table.td [] [ text <| "$" ++ toString stream.income ]
                ]
    in
    Table.table []
        [ Table.thead []
              [ Table.tr []
                    [ Table.th [] [ text "Active?" ]
                    , Table.th [] [ text "Confidence" ]
                    , Table.th [] [ text "Period" ]
                    , Table.th [] [ text "Income" ]
                    ]
              ]
        , Table.tbody []
            <| List.map incomeStreamRow streams]


renderFiles : List FileIncome -> Html a
renderFiles files =
    let
        incomeFile {name,id} =
            li [] [ a
                    [ href ("/api/v1/admin/income-file/" ++ toString id)
                    , download True
                    , downloadAs name
                    ]
                    [ text name ]
                  ]

    in
    div []
        [ h4 [] [ text "Income Files" ]
        , ul []
            <| List.map incomeFile files
        ]



-- JSON DECODER


decoder : Decoder Income
decoder =
    ("type" := string) `andThen` incomeInfo


incomeInfo : String -> Decoder Income
incomeInfo t =
    case t of
        "plaid" ->
            map Plaid
                <| succeed PlaidIncome
                    |: ("last_year" := int)
                    |: ("last_year_pre_tax" := int)
                    |: ("projected_yearly" := int)
                    |: (maybe ("streams" := list incomeStream))
                    |: (maybe ("accounts" := list bankAccount))

        "file" ->
            object1 File
                ("files" := list fileIncome)

        _ ->
            fail (t ++ " is not a recognized type of income data.")


fileIncome : Decoder FileIncome
fileIncome =
    object2 FileIncome
        ("name" := string)
        ("file_id" := int)

incomeStream : Decoder IncomeStream
incomeStream =
    succeed IncomeStream
        |: ("active" := bool)
        |: ("confidence" := float)
        |: ("period" := int)
        |: ("income" := int)

bankAccount : Decoder BankAccount
bankAccount =
    ("type" := string) `andThen` bankAccountInfo

bankAccountInfo : String -> Decoder BankAccount
bankAccountInfo t =
    case t of
        "depository" ->
            object3 Depository
                ("subtype" := depositoryType)
                ("current_balance" := float)
                ("available_balance" := float)

        "credit" ->
            object3 Credit
                ("current_balance" := float)
                ("available_balance" := float)
                ("credit_limit" := float)

        _ ->
            fail (t ++ " is not a recognized bank account type.")

depositoryType : Decoder DepositoryType
depositoryType =
    let
        decoder subtype =
            case subtype of
                "checking" -> Result.Ok Checking
                "savings" -> Result.Ok Savings
                _ -> Result.Err ("expecting either 'checking' or 'savings', got " ++ subtype)
    in
    customDecoder string decoder
