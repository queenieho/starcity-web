module Admin.Application exposing (..)

import Html exposing (..)
import Http
import Task
import String as String
import Json.Decode exposing (..)
import Date as Date exposing (Date)
import Json.Decode.Extra exposing (..)

import Material.Grid exposing (grid, cell, size, Device(..))

import Util exposing (humanDate)
import Admin.Page as Page
import Admin.Application.CommunityFitness as CommunityFitness exposing (CommunityFitness)
import Admin.Application.Income as Income exposing (Income)
import Admin.Application.Common as Common


-- MODEL


type alias Model =
    { applicationId : Int
    , data : Maybe Application
    }

type PetInfo
    = Cat
    | Dog String Int

type alias Application =
    { id : Int
    , name : String
    , email : String
    , phoneNumber : String
    , moveIn : Date
    , properties : List String
    , term : Int
    , completedAt : Date
    , communityFitness : CommunityFitness
    , income : Income
    , address : String
    , pet: Maybe PetInfo
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
            Debug.log (toString application)
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
    let
        topRowSizes =
            case application.pet of
                Just _ -> [ size Desktop 4, size Tablet 6, size Phone 12 ]
                Nothing -> [ size Desktop 6, size Phone 12 ]
    in
    div []
        [ grid []
              [ cell topRowSizes [ basicInfo application ]
              , cell topRowSizes [ moveInInfo application ]
              , cell topRowSizes [ Maybe.map otherInfo application.pet |> Maybe.withDefault (div [] []) ]
              ]
        , grid []
            [ cell [ size All 12]
                  [ CommunityFitness.view application.communityFitness ]
            ]
        , grid []
            [ cell [ size All 12 ]
                  [ Income.view application.income ]
            ]
        ]

basicInfo : Application -> Html a
basicInfo application =
    Common.section "Basic Information"
        <| Common.definitionList
            [ ("Name", application.name)
            , ("Email", application.email)
            , ("Phone Number", application.phoneNumber)
            , ("Completed At", humanDate application.completedAt)
            , ("Current Address", application.address)
            ]

moveInInfo : Application -> Html a
moveInInfo application =
    Common.section "Move-in"
        <| Common.definitionList
            [ ("Desired Move-in Date", humanDate application.moveIn)
            , ("Desired Properties", String.join ", " application.properties)
            , ("Term", toString application.term ++ " months")
            ]

otherInfo : PetInfo -> Html a
otherInfo pet =
    let
        petContent =
            ( "Pet"
             , case pet of
                   Cat -> "Cat"
                   Dog breed weight -> (toString weight) ++ "lb " ++ breed
              )

    in
        Common.section "Other Information"
            <| Common.definitionList
                [ petContent ]

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
        |: ("phone_number" := string)
        |: ("move_in" := date)
        |: ("properties" := list string)
        |: ("term" := int)
        |: ("completed_at" := date)
        |: ("community_fitness" := CommunityFitness.decoder)
        |: ("income" := Income.decoder)
        |: ("address" := string)
        |: (maybe ("pet" := petDecoder))


petDecoder : Decoder PetInfo
petDecoder =
    ("type" := string) `andThen`
        (\t ->
             case t of
                 "cat" ->
                     succeed Cat
                 "dog" ->
                     object2 Dog
                         ("breed" := string)
                         ("weight" := int)
                 _ ->
                     fail ("Expecting either 'cat' or 'dog', got " ++ t)
        )
