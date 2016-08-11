module Admin.Applications exposing (..)

import Html exposing (..)
import Html.App as App
import Html.Events exposing (onClick)
import Html.Attributes exposing (style)
import Http
import Task
import String as String
import Json.Decode exposing (..)
import Json.Decode.Extra exposing (..)
import Date exposing (Date)
import Date.Extra as Date

import RouteUrl.Builder exposing (Builder, builder, path, replacePath, newEntry)
import String exposing (toInt)

import Material
import Material.Color as Color
import Material.Grid exposing (grid, cell, size, Device(..))
import Material.Options as Options exposing (nop, when, css)
import Material.Table as Table

import Json.Decode.Extra exposing (date, maybeNull)
import Admin.Page as Page
import Admin.Application as Application

-- MODEL


type View
    = List
    | Single Int

type SortColumn
    = MoveIn
    | CompletedAt
    | Term

type alias SortOrder = (SortColumn, Maybe Table.Order)

type alias ApplicationRow =
    { id : Int
    , name : String
    , email : String
    , phoneNumber : String
    , moveIn : Date
    , properties : List String
    , term : Int
    , completed : Bool
    , completedAt : Date
    }

type alias Model =
    { applications : List ApplicationRow
    , currentApplication : Maybe Application.Model
    , currentView : View
    , sort : SortOrder
    , mdl : Material.Model
    }


-- INIT

init : (Model, Cmd Msg)
init =
    ( { applications = []
      , currentApplication = Nothing
      , currentView = List
      , sort = (CompletedAt, Just Table.Descending)
      , mdl = Material.model
      }
    , fetchApplications
    )

-- UPDATE


type Msg
    = FetchSucceed (List ApplicationRow)
    | FetchFail Http.Error
    | ShowList
    | ShowApplication Int
    | Reorder SortColumn
    | ApplicationMsg Application.Msg
    | Mdl (Material.Msg Msg)

rotate : SortColumn -> SortOrder -> SortOrder
rotate col sort =
    if (col /= fst sort)
    then (col, Just Table.Ascending)
    else case sort of
             (_, Just Table.Ascending) -> (col, Just Table.Descending)
             (_, Just Table.Descending) -> (col, Nothing)
             (_, Nothing) -> (col, Just Table.Ascending)


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        FetchSucceed applications ->
           { model | applications = applications } ! []

        FetchFail e ->
            model ! []

        ShowList ->
            { model | currentView = List } ! [ fetchApplications ]

        ShowApplication applicationId ->
            let
                (m, fx) = Application.init applicationId
            in
                { model
                    | currentView = Single applicationId
                    , currentApplication = Just m
                } ! [ Cmd.map ApplicationMsg fx ]

        Reorder c ->
            { model | sort = rotate c model.sort } ! []

        ApplicationMsg msg' ->
            maybePassApplicationMessage model msg'

        Mdl msg' ->
            Material.update msg' model

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
            Page.body "Applications"
                <| grid []
                    [ cell [ size All 12 ] [ tableView model ] ]

type WhiteSpace = Normal | NoWrap

ws opt =
    case opt of
        Normal -> css "white-space" "normal"
        NoWrap -> css "white-space" "nowrap"


tableView : Model -> Html Msg
tableView model =
    let
        sort =
            case model.sort of
                (MoveIn, Just Table.Ascending) ->
                    List.sortBy (.moveIn >> Date.toTime)

                (MoveIn, Just Table.Descending) ->
                    List.sortWith (\x y -> reverse
                                       ((.moveIn >> Date.toTime) x)
                                       ((.moveIn >> Date.toTime) y))

                (CompletedAt, Just Table.Ascending) ->
                    List.sortBy (.completedAt >> Date.toTime)

                (CompletedAt, Just Table.Descending) ->
                    List.sortWith (\x y -> reverse
                                       ((.completedAt >> Date.toTime) x)
                                       ((.completedAt >> Date.toTime) y))

                (Term, Just Table.Ascending) ->
                    List.sortBy .term

                (Term,Just Table.Descending) ->
                    List.sortWith (\x y -> reverse (.term x) (.term y))

                (_, Nothing) ->
                    identity

        applicationRow idx row =
            Table.tr []
                [ Table.td [] [ idx + 1 |> toString |> text ]
                , Table.td []
                      [ a [ onClick <| ShowApplication row.id
                          , style [("cursor", "pointer")]
                          ]
                            [ text row.name ]
                      ]
                , Table.td [] [ text row.email ]
                , Table.td [ ws NoWrap ] [ text row.phoneNumber ]
                , Table.td [] [ String.join ", " row.properties |> text]
                , Table.td [] [ toString row.term |> text]
                , Table.td [] [ shortDate row.moveIn |> text]
                , Table.td [] [ shortDate row.completedAt |> text ]
                ]

        (col, currOrder) = model.sort

        colProps col' =
           [ (snd model.sort
             |> Maybe.map Table.sorted
             |> Maybe.withDefault nop) `when` (col' == col)
           , Table.onClick (Reorder col')
           , Color.text Color.accent
           , Table.numeric
           ]

    in
    Table.table []
        [ Table.thead [ ws NoWrap ]
              [ Table.tr []
                    [ Table.th [] [ text "Number" ]
                    , Table.th [] [ text "Name" ]
                    , Table.th [] [ text "Email" ]
                    , Table.th [] [ text "Phone Number" ]
                    , Table.th [] [ text "Properties" ]
                    , Table.th (colProps Term) [ text "Term" ]
                    , Table.th (colProps MoveIn) [ text "Desired Move-In" ]
                    , Table.th (colProps CompletedAt) [ text "Completed At" ]
                    ]
              ]
        , Table.tbody [ ws Normal ]
            ( sort model.applications
                |> (List.indexedMap applicationRow)
            )
        ]

reverse : comparable -> comparable -> Order
reverse x y =
  case compare x y of
    LT -> GT
    GT -> LT
    EQ -> EQ


shortDate : Date -> String
shortDate date =
    Date.toFormattedString "M/dd/y" date


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
    succeed ApplicationRow
        |: ("application_id" := int)
        |: ("name" := string)
        |: ("email" := string)
        |: ("phone_number" := string)
        |: ("move_in" := date)
        |: ("properties" := list string)
        |: ("term" := int)
        |: ("completed" := bool)
        |: ("completed_at" := date)

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
