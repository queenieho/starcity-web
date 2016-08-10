module Admin.Application exposing (..)

import Html exposing (..)
import Http
import Task
import Json.Decode exposing (..)
import Date as Date exposing (Date)
import Json.Decode.Extra exposing (..)

import Material.Card as Card
import Material.Color as Color
import Material.Options as Options
import Material.List as Lists

import Admin.Page as Page
import Admin.Application.CommunityFitness as CommunityFitness exposing (CommunityFitness)
import Admin.Application.Income as Income exposing (Income)
import Admin.Application.Common as Common


-- MODEL


type alias Model =
    { applicationId : Int
    , data : Maybe Application
    }

type alias Application =
    { id : Int
    , name : String
    , email : String
    , completedAt : Date
    , communityFitness : CommunityFitness
    , income : Income
    }


init : Int -> (Model, Cmd Msg)
init id =
    ( Model id Nothing
    , fetchApplication id
    )

-- UPDATE

type Msg
    = FetchSucceed Application
    | FetchFail Http.Error


update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
    case msg of
        FetchSucceed application ->
            ( { model | data = Just application }, Cmd.none )

        FetchFail err ->
            Debug.log (toString err)
            ( model, Cmd.none )


-- VIEW

view : Model -> Html Msg
view model =
    case model.data of
        Just data ->
            Page.body (data.name ++ "'s application") <| content data
        Nothing ->
            Page.body "Loading..." <| div [] []

content : Application -> Html Msg
content application =
    div []
        [ basicInfo application
        , CommunityFitness.view application.communityFitness
        , Income.view application.income
        ]

basicInfo : Application -> Html a
basicInfo application =
    Common.section "Basic Information"
        <| Common.definitionList
            [ ("Name", application.name)
            , ("Email", application.email)
            , ("Completed At", toString application.completedAt)
            ]

white : Options.Property c m
white =
    Color.text Color.white

-- HTTP


fetchApplication : Int -> Cmd Msg
fetchApplication applicationId =
    let
        url = "/api/v1/admin/applications/" ++ toString applicationId
    in
        Task.perform FetchFail FetchSucceed (Http.get decoder url)


decoder : Decoder Application
decoder =
    succeed Application
        |: ("id" := int)
        |: ("name" := string)
        |: ("email" := string)
        |: ("completed_at" := date)
        |: ("community_fitness" := CommunityFitness.decoder)
        |: ("income" := Income.decoder)
