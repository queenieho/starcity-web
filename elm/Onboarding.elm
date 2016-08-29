module Onboarding exposing (..)

-- import Html.App as App exposing (map)

import Html exposing (..)
import Html.App as App
import Html.Attributes exposing (style, class, id, src, alt)
import Html.Events exposing (onClick)
import Http
import Task
import Json.Decode exposing (..)
import Navigation
import RouteUrl as Routing exposing (UrlChange)
import RouteUrl.Builder as Builder exposing (Builder)
import Material
import Material.Color as Color
import Material.Grid exposing (grid, cell, size, offset, Device(..))
import Material.Helpers exposing (map1st, map2nd)
import Material.Layout as Layout
import Material.List as Lists
import Material.Options as Options
-- import Material.Typography as Typo
import Page as Page
import Onboarding.Loading
import Onboarding.Begin
import Onboarding.SecurityDeposit as SecurityDeposit


-- MAIN


main : Program Never
main =
    Routing.program
        { delta2url = delta2url
        , location2messages = location2messages
        , init = init
        , view = view
        , update = update
        , subscriptions = subscriptions
        }



-- MODEL


type Step
    = Begin String
    | SecurityDeposit


type alias Model =
    { currentView : Maybe Step
    , begin : Onboarding.Begin.Model
    , securityDeposit : SecurityDeposit.Model
    , mdl : Material.Model
    }


init : ( Model, Cmd Msg )
init =
    let
        materialModel =
            Layout.setTabsWidth 400 Material.model

        ( oModel, oCmd ) =
            SecurityDeposit.init
    in
        ( Model Nothing Onboarding.Begin.init oModel materialModel
        , Cmd.batch
            [ Cmd.none
            , Layout.sub0 Mdl
            , Cmd.map SecurityDepositMsg oCmd
            ]
        )



-- ACTION, UPDATE


type Msg
    = ShowStep Step
      -- Tasks for determining current state
    | FetchStep
    | FetchStepSucceed Step
    | FetchStepFail Http.Error
      -- Component Views
    | BeginMsg Onboarding.Begin.Msg
    | SecurityDepositMsg SecurityDeposit.Msg
      -- MDL
    | Mdl (Material.Msg Msg)


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        ShowStep step ->
            ( { model | currentView = Just step }
            , Cmd.none
            )

        FetchStep ->
            ( model, fetchStep )

        FetchStepSucceed step ->
            update (ShowStep step) model

        FetchStepFail err ->
            Debug.log
                (toString err)
                ( model, Cmd.none )

        SecurityDepositMsg msg' ->
            SecurityDeposit.update msg' model.securityDeposit
                |> map1st (\model' -> { model | securityDeposit = model' })
                |> map2nd (Cmd.map SecurityDepositMsg)

        BeginMsg msg' ->
            Onboarding.Begin.update msg' model.begin
                |> map1st (\model' -> { model | begin = model' })
                |> map2nd (Cmd.map BeginMsg)



        -- Boilerplate: Mdl action handler
        Mdl msg' ->
            Material.update msg' model



-- VIEW


view : Model -> Html Msg
view model =
    let
        view =
            case model.currentView of
                Nothing ->
                    Onboarding.Loading.view

                Just (Begin community) ->
                    container ("Welcome to " ++ community ++ "!")
                        <| App.map BeginMsg (Onboarding.Begin.view model.begin)

                Just SecurityDeposit ->
                    App.map SecurityDepositMsg (SecurityDeposit.view model.securityDeposit)
    in
        Layout.render Mdl
            model.mdl
            [-- Layout.fixedHeader
            ]
            { header =
                []
                --header model
            , drawer = []
            , tabs = ( [], [ Color.background Color.primaryDark ] )
            , main = [ view ]
            }


container : String -> Html m -> Html m
container title content =
    Options.div
        Page.boxed
        [ grid []
            [ cell
                [ size Desktop 2 ]
                [ navList ]
            , cell
                [ size Desktop 8
                , size Tablet 8
                , size Phone 4
                ]
                [ Options.div
                    []
                    [ h1 [] [ text title ] ]
                , div [] [ content ]
                ]
            ]
        ]


-- TODO: Active/Inactive states
navList : Html m
navList =
    Lists.ul []
        [ Lists.li [] [ Lists.content [] [ text "Welcome!" ] ]
        , Lists.li [] [ Lists.content [] [ text "Security Deposit" ] ]
        ]


logo : List (Html a)
logo =
    [ img
        [ id "header-logo"
        , src "/assets/img/starcity-brand-icon-white.png"
        , alt "Starcity Logo"
        ]
        []
    , span [] [ text "Starcity" ]
    ]


header : Model -> List (Html Msg)
header model =
    -- TODO: Left-right spacing...get to be in line w/ materialize looks
    [ Layout.row []
        [ Layout.title []
            [ a
                [ class "brand-logo"
                , onClick FetchStep
                , style
                    [ ( "cursor", "pointer" )
                    , ( "color", "white" )
                    ]
                ]
                logo
            ]
        , Layout.spacer
        , Layout.navigation []
            [ Layout.link
                [ Layout.href "/logout" ]
                [ span [] [ text "Log Out" ] ]
            ]
        ]
    ]



-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions model =
    let
        securityDepositSubs =
            Sub.map SecurityDepositMsg (SecurityDeposit.subscriptions model.securityDeposit)
    in
        Sub.batch
            [ Layout.subs Mdl model.mdl
            , securityDepositSubs
            ]



-- HTTP


fetchStep : Cmd Msg
fetchStep =
    let
        url =
            "/api/v1/onboarding/step"
    in
        Task.perform FetchStepFail FetchStepSucceed (Http.get stepDecoder url)


stepDecoder : Decoder Step
stepDecoder =
    ("step" := string)
        `andThen`
            (\step ->
                case step of
                    "begin" ->
                        object1 Begin
                            ("community" := string)

                    _ ->
                        fail <| "DEBUG: Expecting `begin`, got " ++ step
            )



-- ROUTING


delta2url : Model -> Model -> Maybe UrlChange
delta2url previous current =
    Maybe.map Builder.toUrlChange <|
        Maybe.map (Builder.prependToPath [ "onboarding" ]) <|
            delta2builder previous current


delta2builder : Model -> Model -> Maybe Builder
delta2builder previous current =
    case current.currentView of
        Nothing ->
            Builder.builder
                |> Builder.replacePath [ "" ]
                |> Just

        Just (Begin _) ->
            Builder.builder
                |> Builder.replacePath [ "begin" ]
                |> Just

        Just SecurityDeposit ->
            Builder.builder
                |> Builder.replacePath [ "security-deposit" ]
                |> Just


location2messages : Navigation.Location -> List Msg
location2messages location =
    builder2messages <| Builder.fromUrl location.href


builder2messages : Builder -> List Msg
builder2messages builder =
    let
        reset =
            [ FetchStep ]
    in
        case Builder.path builder of
            first :: rest ->
                let
                    subBuilder =
                        Builder.replacePath rest builder
                in
                    case first of
                        -- Trick to essentially skip the base url of /admin
                        "onboarding" ->
                            builder2messages <|
                                Builder.replacePath rest builder

                        -- NOTE: In practice, the appropriate step will be
                        -- determined by progress via onboarding as a whole, so
                        -- any matched routes are purely for dev purposes atm
                        -- "security-deposit" ->
                        --     -- (ShowView SecurityDeposit) :: List.map SecurityDepositMsg (.builder2messages subBuilder)
                        --     [ ShowStep SecurityDeposit ]

                        -- Show loading screen while we figure out where we're supposed to be.
                        _ ->
                            reset

            -- Error or not found?
            _ ->
                reset
