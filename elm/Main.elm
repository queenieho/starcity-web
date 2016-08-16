module Main exposing (..)

-- import Html.App as App exposing (map)
import Html exposing (..)
import Html.Attributes exposing (style, class, id, src, alt)
import Html.Events exposing (onClick)

import Navigation
import RouteUrl as Routing exposing (UrlChange)
import RouteUrl.Builder as Builder exposing (Builder)

import Material
import Material.Color as Color
import Material.Options as Options -- exposing (css, when)
import Material.Typography as Typography
import Material.Layout as Layout
-- import Material.Helpers exposing (lift)


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


type alias Model =
    { currentView : View
    , mdl : Material.Model -- boilerplate model for Mdl components
    }


init : (Model, Cmd Msg)
init =
    let
        materialModel =
            Layout.setTabsWidth 400 Material.model
    in
        ( Model Home materialModel
        , Cmd.batch
            [ Cmd.none
            , Layout.sub0 Mdl
            ]
        )

-- ACTION, UPDATE


type Msg
    = ShowView View
    | Mdl (Material.Msg Msg)

update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        ShowView view ->
            ( { model | currentView = view }
            , Cmd.none
            )


        -- Boilerplate: Mdl action handler
        Mdl msg' ->
            Material.update msg' model


-- VIEW

e404 : Model -> Html Msg
e404 _ =
  div []
    [ Options.styled Html.h1
        [ Options.cs "mdl-typography--display-4"
        , Typography.center
        ]
        [ text "404" ]
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
    [ Layout.row []
          [ Layout.title []
                [ a [ class "brand-logo"
                    , onClick (ShowView Home)
                    , style [ ("cursor", "pointer")
                            , ("color", "white")]
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

view : Model -> Html Msg
view model =
    let
        view =
            case model.currentView of
                Home ->
                    Html.div [] [ text "Hello, World!" ]

    in
    Layout.render Mdl model.mdl
        [ Layout.fixedHeader
        ]
    { header = header model
    , drawer = []
    , tabs = ([], [ Color.background Color.primaryDark ])
    , main = [ view ]
    }


-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions model =
    Layout.subs Mdl model.mdl


-- ROUTING



delta2url : Model -> Model -> Maybe UrlChange
delta2url previous current =
    Maybe.map Builder.toUrlChange
        <| Maybe.map (Builder.prependToPath ["me"])
        <| delta2builder previous current

delta2builder : Model -> Model -> Maybe Builder
delta2builder previous current =
    case current.currentView of
        Home ->
            Builder.builder
                |> Builder.replacePath [""]
                |> Just

        -- Applications ->
        --     Applications.delta2builder previous.applications current.applications
        --         |> Maybe.map (Builder.prependToPath ["applications"])


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
                    -- "admin" ->
                    --     builder2messages
                    --         <| Builder.replacePath rest builder

                    -- "applications" ->
                    --     (ShowView Applications) :: List.map ApplicationsMsg (Applications.builder2messages subBuilder)

                    -- Error?
                    _ ->
                        [ ShowView Home ]

        -- Error?
        _ ->
            [ ShowView Home ]
