module Admin.Applications exposing (..)

import Html exposing (..)
import Html.App as App
import Html.Events exposing (onClick)
import Html.Attributes exposing (style)
import Http
import Task
import Json.Decode exposing (..)
import Date as Date exposing (Date)

import RouteUrl.Builder exposing (Builder, builder, path, replacePath, newEntry)
import String exposing (toInt)

import Material.Table as Table

import Json.Decode.Extra exposing (date, maybeNull)
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
    = FetchSucceed (List ApplicationRow)
    | FetchFail Http.Error
    | ShowList
    | ShowApplication Int
    | ApplicationMsg Application.Msg


-- UPDATE


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        FetchSucceed applications ->
            ( { model | applications = applications }, Cmd.none )

        FetchFail _ ->
            (Model model.applications model.currentApplication model.currentView, Cmd.none)

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

-- VIEW

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
        , Table.td [] [ Maybe.map toString completedAt
                      |> Maybe.withDefault "Incomplete"
                      |> text ]
        ]

view : Model -> Html Msg
view model =
    case model.currentView of
        Single _ ->
            case model.currentApplication of
                Just application ->
                    App.map ApplicationMsg (Application.view application)

                Nothing ->
                    Page.body "Loading..." (div [] [])

        List ->
            Page.body "Applications" <| listView model

listView : Model -> Html Msg
listView model =
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

-- HTTP


fetchApplications : Cmd Msg
fetchApplications =
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
                ("application_id" := int)
                ("name" := string)
                ("email" := string)
                ("completed_at" := maybeNull date)
                ("completed" := bool)

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
