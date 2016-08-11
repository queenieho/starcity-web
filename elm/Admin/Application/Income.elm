module Admin.Application.Income exposing (..)

import Html exposing (..)
import Html.Attributes exposing (href, download, downloadAs)

import Json.Decode exposing (..)
import Json.Decode.Extra exposing (..)

import Material.Table as Table

import Admin.Application.Common as Common

-- MODEL

type alias IncomeStream =
    { active : Bool
    , confidence : Float
    , period : Int
    , income : Int
    }

type alias BankAccount =
    { accountType : String
    , subType : String
    , currentBalance : Float
    , availableBalance : Maybe Float
    , creditLimit : Maybe Float
    }

type alias PlaidIncome =
    { lastYear : Maybe Int
    , lastYearPreTax : Maybe Int
    , projectedYearly : Maybe Int
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
    let
        dollar amt =
            Maybe.map (\x -> "$" ++ toString x) amt
                |> Maybe.withDefault "Unknown"
    in
    div []
        [ div []
              [ h4 [] [ text "Basic" ]
              , Common.definitionList
                  [ ("Last Year's Income", dollar plaid.lastYear)
                  , ("Last Year's Income, Pre-Tax", dollar plaid.lastYearPreTax)
                  , ("Projected Yearly Income", dollar plaid.projectedYearly)
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
        row t st curr avail lim =
            Table.tr []
                [ Table.td [] [ text t ]
                , Table.td [] [ text st ]
                , Table.td [] [ text curr ]
                , Table.td [] [ text avail ]
                , Table.td [] [ text lim ]
                ]

        maybeDollar amt =
            Maybe.map (\x -> "$" ++ toString x) amt
                |> Maybe.withDefault "N/A"

        accountRow account =
            row account.accountType
                account.subType
                    ("$" ++ toString account.currentBalance)
                    (maybeDollar account.availableBalance)
                    (maybeDollar account.creditLimit)

    in
    div []
        [ h4 [] [ text "Bank Accounts" ]
        , Table.table []
            [ Table.thead []
                  [ Table.tr []
                        [ Table.th [] [ text "Type" ]
                        , Table.th [] [ text "Subtype" ]
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
                    |: (maybe ("last_year" := int))
                    |: (maybe ("last_year_pre_tax" := int))
                    |: (maybe ("projected_yearly" := int))
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
    succeed BankAccount
        |: ("type" := string)
        |: ("subtype" := string)
        |: ("current_balance" := float)
        |: (maybe ("available_balance" := float))
        |: (maybe ("credit_limit" := float))
