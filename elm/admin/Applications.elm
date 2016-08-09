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
import Admin.Application as Application

-- MODEL


type View
    = List
    | Single Int

type alias Model =
    { applications : List ApplicationRow
    , currentApplication : Maybe Application.Model
    , currentView : View
    }

type alias ApplicationRow =
    { id : Int
    , name : String
    , email : String
    , completedAt : Maybe Date
    , completed: Bool
    }

-- INIT

init : (Model, Cmd Msg)
init =
    ( Model [] Nothing List
    , fetchApplications
    )

-- MESSAGES


type Msg
    = FetchListSucceed (List ApplicationRow)
    | FetchListFail Http.Error
    -- | FetchApplicationSucceed Application
    -- | FetchApplicationFail Http.Error
    | ShowList
    | ShowApplication Int
    | ApplicationMsg Application.Msg


-- UPDATE


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        FetchListSucceed applications ->
            ( { model | applications = applications }, Cmd.none )

        FetchListFail _ ->
            (Model model.applications model.currentApplication model.currentView, Cmd.none)

        -- FetchApplicationSucceed application ->
        --     ( { model | currentApplication = Just application}, Cmd.none )

        -- FetchApplicationFail err ->
        --     (model, Cmd.none)
        ShowList ->
            ( { model | currentView = List }, fetchApplications )

        ShowApplication applicationId ->
            let
                (m, fx) = Application.init applicationId
            in
                ( { model
                      | currentView = Single applicationId
                      , currentApplication = Just m
                  }
                , Cmd.map ApplicationMsg fx
                )

        ApplicationMsg msg' ->
            maybePassApplicationMessage model msg'

maybePassApplicationMessage : Model -> Application.Msg -> (Model, Cmd Msg)
maybePassApplicationMessage model msg =
    case model.currentApplication of
        Just model' ->
            let
                (m, fx) = Application.update msg model'
            in
                ( { model | currentApplication = Just m }
                , Cmd.map ApplicationMsg fx
                )

        Nothing ->
            ( model, Cmd.none )


-- TODO: Understand how to do this
-- passApplicationMessage : Application.Msg -> Model -> Application.Model -> Maybe (Model, Cmd Msg)
-- passApplicationMessage msg model subModel =
--     let
--         (m, fx) = Application.update msg subModel
--     in
--         ( { model | currentApplication = Just m }
--         , Cmd.map ApplicationMsg fx
--         )
--         |> Just

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
            Single _ ->
                case model.currentApplication of
                    Just id ->
                        Page.body ("Showing Application w/ id " ++ (toString id)) (div [] [])

                    Nothing ->
                        Page.body "Loading..." (div [] [])

            List ->
                Page.body "Applications" (div [] [ table ])

-- HTTP


-- fetchApplication : Int -> Cmd Msg
-- fetchApplication applicationId =
--     let
--         url = "/api/v1/admin/applications/" ++ toString applicationId
--     in
--         Task.perform FetchApplicationFail FetchApplicationSucceed (Http.get applicationDecoder2 url)


-- applicationDecoder2 : Decoder Application
-- applicationDecoder2 =
--     at ["id"] int

fetchApplications : Cmd Msg
fetchApplications =
    let
        url = "/api/v1/admin/applications"
    in
        Task.perform FetchListFail FetchListSucceed (Http.get decodeApplications url)

decodeApplications : Decoder (List ApplicationRow)
decodeApplications =
    list applicationDecoder

applicationDecoder : Decoder ApplicationRow
applicationDecoder =
    object5 ApplicationRow
                ("application_id" := int)
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
                Single applicationId ->
                    [ toString applicationId ]

                List ->
                    []
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
