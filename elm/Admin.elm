module Admin exposing (..)

import Html.App as App exposing (map)
import Html exposing (..)
import Html.Lazy
-- import Html.Attributes exposing (href, class, style)
-- import Array exposing (Array)
-- import Dict exposing (Dict)
-- import String

import Navigation
import RouteUrl as Routing exposing (UrlChange)
import RouteUrl.Builder as Builder exposing (Builder)

import Material
import Material.Color as Color
import Material.Options as Options -- exposing (css, when)
import Material.Typography as Typography
import Material.Layout as Layout
import Material.Helpers exposing (lift)

import Admin.Applications as Applications


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
    | Applications

type alias Model =
    { currentView : View
    , applications : Applications.Model
    , mdl : Material.Model -- boilerplate model for Mdl components
    }


init : (Model, Cmd Msg)
init =
    let
        materialModel =
            Layout.setTabsWidth 400 Material.model

        (applicationsModel, applicationsFx) =
            Applications.init
    in
        ( Model Home applicationsModel materialModel
        , Cmd.batch
            [ Cmd.map ApplicationsMsg applicationsFx
            , Layout.sub0 Mdl
            ]
        )

-- ACTION, UPDATE


type Msg
    = ShowView View
    | SelectTab Int
    | ApplicationsMsg Applications.Msg
    | Mdl (Material.Msg Msg)


-- TODO: Better way to do this?
viewForTabIndex : Int -> View
viewForTabIndex index =
    case index of
        0 -> Applications

        -- Catch-all
        _ -> Home

tabIndexForView : View -> Int
tabIndexForView view =
    case view of
        Home -> -1
        Applications -> 0

update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        ShowView view ->
            ( { model | currentView = view }
            , Cmd.none
            )

        SelectTab t ->
            -- let
            --     (m, fx) = Applications.update Applications.ShowList model.applications
            -- in
            ( { model
                  | currentView = viewForTabIndex t
                  -- , applications = m
              }
            , Cmd.none
            )

        ApplicationsMsg msg' ->
            lift .applications (\m sm -> {m | applications = sm}) ApplicationsMsg Applications.update msg' model

        -- Boilerplate: Mdl action handler
        Mdl msg' ->
            Material.update msg' model


-- VIEW

e404 : Model -> Html Msg
e404 _ =
  div
    [
    ]
    [ Options.styled Html.h1
        [ Options.cs "mdl-typography--display-4"
        , Typography.center
        ]
        [ text "404" ]
    ]

header : Model -> List (Html Msg)
header model =
    [ Layout.row []
          [ Layout.title [] [ text "Starcity Admin" ]
          , Layout.spacer
          , Layout.navigation []
              [ Layout.link
                    [ Layout.href "/logout" ]
                    [ span [] [ text "Log Out" ] ]
              ]
          ]
    ]

view : Model -> Html Msg
view = Html.Lazy.lazy view'

view' : Model -> Html Msg
view' model =
    let
        view =
            case model.currentView of
                Home ->
                    div [] [ h3 [] [ text "Choose a tab above." ] ]

                Applications ->
                    map ApplicationsMsg (Applications.view model.applications)
    in
    Layout.render Mdl model.mdl
        [ Layout.fixedHeader
        , Layout.selectedTab (tabIndexForView model.currentView)
        , Layout.onSelectTab SelectTab
        ]
    { header = header model
    , drawer = []
    , tabs = (tabTitles, [ Color.background (Color.color Color.Green Color.S400) ])
    , main = [ view ]
    }


-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions model =
    Layout.subs Mdl model.mdl


-- ROUTING


tabs : List (String, String, Model -> Html Msg)
tabs =
    [ ("Applications", "applications", .applications >> Applications.view >> App.map ApplicationsMsg) ]

tabTitles : List (Html a)
tabTitles =
    List.map (\ (x,_,_) -> text x) tabs

-- tabViews : Array (Model -> Html Msg)
-- tabViews =
--     List.map (\ (_,_,x) -> x) tabs
--         |> Array.fromList

-- tabUrls : Array String
-- tabUrls =
--     List.map (\ (_,url,_) -> url) tabs
--         |> Array.fromList


delta2url : Model -> Model -> Maybe UrlChange
delta2url previous current =
    Maybe.map Builder.toUrlChange
        <| Maybe.map (Builder.prependToPath ["admin"])
        <| delta2builder previous current

delta2builder : Model -> Model -> Maybe Builder
delta2builder previous current =
    case current.currentView of
        Home ->
            Nothing

        Applications ->
            -- case (previous.currentView, current.currentView) of
            --     (View.Application, View.List) ->
            --         Just Builder.replacePath ["applications"]

            --     _ ->
            Applications.delta2builder previous.applications current.applications
                |> Maybe.map (Builder.prependToPath ["applications"])


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
                    "admin" ->
                        builder2messages
                            <| Builder.replacePath rest builder

                    "applications" ->
                        (ShowView Applications) :: List.map ApplicationsMsg (Applications.builder2messages subBuilder)

                    -- Error?
                    _ ->
                        [ ShowView Home ]

        -- Error?
        _ ->
            [ ShowView Home ]
