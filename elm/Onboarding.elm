module Onboarding exposing (..)

-- import Html.App as App exposing (map)

import Html exposing (..)
import Html.App as App
import Html.Attributes exposing (style, class, id, src, alt)
import Html.Events exposing (onClick)

import Navigation
import RouteUrl as Routing exposing (UrlChange)
import RouteUrl.Builder as Builder exposing (Builder)

import Material
import Material.Color as Color
import Material.Helpers exposing (map1st, map2nd)
import Material.Layout as Layout

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


type View
    = Home
    | SecurityDeposit


type alias Model =
    { currentView : View
    , securityDeposit : SecurityDeposit.Model
    , mdl : Material.Model
    }


init : ( Model, Cmd Msg )
init =
    let
        materialModel =
            Layout.setTabsWidth 400 Material.model

        (oModel, oCmd) =
            SecurityDeposit.init
    in
        ( Model SecurityDeposit oModel materialModel
        , Cmd.batch
            [ Cmd.none
            , Layout.sub0 Mdl
            , Cmd.map SecurityDepositMsg oCmd
            ]
        )



-- ACTION, UPDATE


type Msg
    = ShowView View
    | SecurityDepositMsg SecurityDeposit.Msg
    | Mdl (Material.Msg Msg)


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        ShowView view ->
            ( { model | currentView = view }
            , Cmd.none
            )

        SecurityDepositMsg msg' ->
            SecurityDeposit.update msg' model.securityDeposit
                |> map1st (\model' -> { model | securityDeposit = model' })
                |> map2nd (Cmd.map SecurityDepositMsg)

        -- Boilerplate: Mdl action handler
        Mdl msg' ->
            Material.update msg' model



-- VIEW


view : Model -> Html Msg
view model =
    let
        view =
            case model.currentView of
                Home ->
                    div [] [ text "Hello, World!" ]

                SecurityDeposit ->
                    App.map SecurityDepositMsg (SecurityDeposit.view model.securityDeposit)
    in
        Layout.render Mdl
            model.mdl
            [ Layout.fixedHeader
            ]
            { header = header model
            , drawer = []
            , tabs = ( [], [ Color.background Color.primaryDark ] )
            , main = [ view ]
            }


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
    [ Layout.row []
        [ Layout.title []
            [ a
                [ class "brand-logo"
                , onClick (ShowView Home)
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



-- ROUTING


delta2url : Model -> Model -> Maybe UrlChange
delta2url previous current =
    Maybe.map Builder.toUrlChange <|
        Maybe.map (Builder.prependToPath [ "onboarding" ]) <|
            delta2builder previous current


delta2builder : Model -> Model -> Maybe Builder
delta2builder previous current =
    case current.currentView of
        Home ->
            Builder.builder
                |> Builder.replacePath [ "" ]
                |> Just

        SecurityDeposit ->
            Builder.builder
                |> Builder.replacePath [ "security-deposit" ]
                |> Just


location2messages : Navigation.Location -> List Msg
location2messages location =
    builder2messages <| Builder.fromUrl location.href


builder2messages : Builder -> List Msg
builder2messages builder =
    case Builder.path builder of
        first :: rest ->
            let
                subBuilder =
                    Builder.replacePath rest builder
            in
                case first of
                    -- Trick to essentially skip the base url of /admin
                    "onboarding" ->
                        builder2messages
                            <| Builder.replacePath rest builder
                    "security-deposit" ->
                        -- (ShowView SecurityDeposit) :: List.map SecurityDepositMsg (.builder2messages subBuilder)
                        [ ShowView SecurityDeposit ]
                    -- Error?
                    _ ->
                        [ ShowView Home ]

        -- Error?
        _ ->
            [ ShowView Home ]
