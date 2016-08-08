module Admin.Applications exposing (..)

import Html exposing (..)
import Html.Events exposing (onClick)
import Html.Attributes exposing (style)
import Http
import Task
import Json.Decode  exposing (..)
import Date as Date exposing (Date)

import RouteUrl.Builder exposing (Builder, builder, path, replacePath, newEntry)
import String exposing (toInt)

import Material.Table as Table

import Admin.Page as Page

-- MODEL


type View
    = List
    | Single Int

type alias Model =
    { applications : List ApplicationRow
    , currentApplication : Maybe Application
    , currentView : View
    }

type alias ApplicationRow =
    { id : Int
    , name : String
    , email : String
    , completedAt : Maybe Date
    , completed: Bool
    }

type alias Application = Int

-- INIT

init : Int -> (Model, Cmd Msg)
init num =
    ( Model [] Nothing List
    , fetchApplications num
    )

-- MESSAGES


type Msg
    = Fetch
    | FetchSucceed (List ApplicationRow)
    | FetchFail Http.Error
    | ShowList
    | ShowApplication Int


-- UPDATE


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        Fetch ->
            (model, fetchApplications 0)

        FetchSucceed applications ->
            (Model applications model.currentApplication List, Cmd.none)

        FetchFail _ ->
            (model, Cmd.none)

        ShowList ->
            ( { model | currentView = List }, Cmd.none )

        ShowApplication applicationId ->
            ( { model | currentView = Single applicationId }, Cmd.none ) -- TODO: Network call



-- VIEW

completedAtText : Maybe Date -> String
completedAtText date =
    case date of
        Just date ->
            toString date

        Nothing ->
            "Incomplete"

applicationRow : ApplicationRow -> Html Msg
applicationRow {id,name,email,completedAt,completed} =
    Table.tr []
        [ Table.td []
              [ a [ onClick <| ShowApplication id
                  , style [("cursor", "pointer")]
                  ]
                    [ text (toString id) ]
              ]
        , Table.td [] [ text name ]
        , Table.td [] [ text email ]
        , Table.td [] [ text (toString completed) ]
        , Table.td [] [ text <| completedAtText completedAt ]
        ]

view : Model -> Html Msg
view model =
    let
        table =
            Table.table []
                [ Table.thead []
                      [ Table.tr []
                            [ Table.th [] [ text "ID" ]
                            , Table.th [] [ text "Name" ]
                            , Table.th [] [ text "Email" ]
                            , Table.th [] [ text "Completed?" ]
                            , Table.th [] [ text "Completed At" ]
                            ]
                      ]
                , Table.tbody []
                     (List.map applicationRow model.applications)
                ]


    in
        case model.currentView of
            Single id ->
                Page.body ("Showing Single Application w/ id " ++ (toString id)) (div [] [])

            List ->
                Page.body "Applications" (div [] [ table ])

-- HTTP

fetchApplications : Int -> Cmd Msg
fetchApplications num =
    let
        url = "/api/v1/admin/applications"
    in
        Task.perform FetchFail FetchSucceed (Http.get decodeApplications url)

decodeApplications : Decoder (List ApplicationRow)
decodeApplications =
    list applicationDecoder

applicationDecoder : Decoder ApplicationRow
applicationDecoder =
    object5 ApplicationRow
                ("account_id" := int)
                ("name" := string)
                ("email" := string)
                ("completed_at" := nullOr date)
                ("completed" := bool)

-- maybeDate : Decoder (Maybe Date)
-- maybeDate =
--     string `andThen` \val ->
--         case Date.fromString val of
--             Err err -> Nothing
--             Ok date -> succeed <| Just date

date : Decoder Date
date =
    string `andThen` \val ->
        case Date.fromString val of
            Err err -> fail err
            Ok date -> succeed <| date

nullOr : Decoder a -> Decoder (Maybe a)
nullOr decoder =
    oneOf
    [ null Nothing
    , map Just decoder
    ]

-- ROUTING

delta2builder : Model -> Model -> Maybe Builder
delta2builder previous current =
    let
        path =
            case current.currentView of
                Single applicationId -> [ toString applicationId ]
                List -> []
    in
        builder
            |> replacePath path
            |> Just

builder2messages : Builder -> List Msg
builder2messages builder =
    case path builder of
        first :: rest ->
            case toInt first of
                Ok value ->
                    [ ShowApplication value ]

                Err _ ->
                    -- Non-integer...just show all applications
                    [ ShowList ]

        _ ->
            -- No longer viewing an application.
            [ ShowList ]
